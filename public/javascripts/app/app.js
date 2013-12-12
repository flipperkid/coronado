Ext.Loader.setConfig({
    enabled: true,
    disableCaching: false
});
Ext.syncRequire('Ext.data.Request');
Ext.syncRequire('Ext.data.proxy.Rest');
Ext.syncRequire('Ext.data.writer.Json');
Ext.syncRequire('Ext.window.MessageBox');

Ext.define('Quote', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'symbol', type: 'string'},
        {name: 'close', type: 'decimal'},
        {name: 'date', type: 'date', dateFormat: 'time', serialize: function(value, record) {
            return Ext.Date.format(value, 'c');
        }}
    ]
});

Ext.define('TimeseriesValue', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'closeValue', type: 'decimal'},
        {name: 'aggrGain', type: 'decimal'},
        {name: 'returnRate', type: 'decimal'},
        {name: 'returnPct', type: 'decimal', convert: function(value, record) {
            return (record.get('returnRate') / (value !== '' ? value.get('returnRate') : 1) - 1)*100;
        }},
        {name: 'annualizedReturn', type: 'decimal', convert: function(value, record) {
            var currDate = record.get('date');
            var daysElapsed = value !== '' ? daysBetween(value.get('date'), currDate) :
                (1 + daysBetween(record.get('startDate'), currDate));
            return (Math.pow(record.get('returnRate'), 365.0 / daysElapsed) - 1)*100;
        }},
        {name: 'date', type: 'date', dateFormat: 'time', serialize: function(value, record) {
            return Ext.Date.format(value, 'c');
        }},
        {name: 'startDate', type: 'date', dateFormat: 'time', serialize: function(value, record) {
            return Ext.Date.format(value, 'c');
        }}
    ]
});

Ext.define('Position', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'costBasis', type: 'decimal'},
        {name: 'closeValue', type: 'decimal'},
        {name: 'profitLoss', type: 'decimal', persist: false, convert: function(value, record) {
            return value !== '' ? value : record.get('closeValue') - record.get('costBasis');
        }},
        {name: 'shares', type: 'decimal'},
        {name: 'closed', type: 'boolean'},
        {name: 'openDate', type: 'date', dateFormat: 'time', serialize: function(value, record) {
            return Ext.Date.format(value, 'c');
        }},
        {name: 'closeDate', type: 'date', dateFormat: 'time', serialize: function(value, record) {
            return Ext.Date.format(value, 'c');
        }},
        {name: 'termLength', type: 'int', persist: false, convert: function(value, record) {
            return value !== '' ? value : daysBetween(record.get('openDate'), record.get('closeDate')) + 1;
        }},
        {name: 'symbol', type: 'string'},
        {name: 'cusip', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'securityType', type: 'string'},
        {name: 'return', type: 'decimal', persist: false, convert: function(value, record) {
            return value !== '' ? value : record.get('profitLoss')/record.get('costBasis');
        }},
        {name: 'annualizedReturn', type: 'decimal', persist: false, convert: function(value, record) {
            return value !== '' ? value : Math.pow(1 + record.get('return'), 365/record.get('termLength'));
        }}
    ],
    hasMany: [
        {model: 'Quote', name: 'quotes', persist: false},
        {model: 'TimeseriesValue', name: 'timeseriesData', persist: false}
    ],
    proxy: {
        type: 'ajax',
        url: '/positions',
        reader: {
            type: 'json'
        }
    }
});

Ext.define('PositionTag', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'tag', type: 'string'},
        {name: 'annualizedReturn', type: 'decimal', persist: false},
        {name: 'return', type: 'decimal', persist: false},
        {name: 'costBasis', type: 'decimal', persist: false},
        {name: 'profitLoss', type: 'decimal', persist: false}
    ],
    hasMany: {model: 'Position', name: 'positions'},
    proxy: {
        type: 'rest',
        url: '/tags',
        reader: {
            type: 'json'
        },
        writer: {
            type: 'json'
        }
    }
});

Ext.define('Bookkeeping', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'symbol', type: 'string'},
        {name: 'date', type: 'date', dateFormat: 'time', serialize: function(value, record) {
            return Ext.Date.format(value, 'c');
        }},
        {name: 'cusip', type: 'string'},
        {name: 'description', type: 'string'},

        {name: 'amount', type: 'decimal'},
        {name: 'quantity', type: 'decimal'}
    ],
    hasOne: {model: 'BookkeepingResolution', name: 'resolution', associationKey: 'resolution'},
    proxy: {
        type: 'rest',
        url: '/bookkeeping',
        reader: {
            type: 'json'
        },
        writer: {
            type: 'json'
        }
    }
});

Ext.define('BookkeepingResolution', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'type', type: 'string'},
        {name: 'parentCusip', type: 'string'},
        {name: 'splitRatio', type: 'decimal'}
    ]
});

Ext.onReady(function() {
    defineOverrides();
    loadPerformance();
});

var loadPerformance = function() {

    var bodyDiv = Ext.create('Ext.container.Container', {
        renderTo: Ext.getBody()
    });
    var btnDiv = Ext.create('Ext.container.Container', {
        layout: 'hbox'
    });
    var viewDiv = Ext.create('Ext.container.Container', {
        layout: 'hbox',
        defaults: {
            flex: 1
        }
    });
    var rightCol = Ext.create('Ext.container.Container', {
        layout: 'vbox',
        defaults: {
            width: '100%'
        }
    });

    aggrPerfStore = createAggrPerfStore();
    taggedPerfStore = createTagStore();
    performanceStore = createPositionStore();
    createBookkeepingWindow();
    bookkeepingStore = createBookkeepingStore();

    // Visualization
    annualizedReturnChart = Ext.create('Ext.chart.Chart', {
        height: 300,
        animate: true,
        store: Ext.create('Ext.data.Store', {
            model: 'TimeseriesValue',
            proxy: {
                type: 'memory'
            }
        }),
        axes: [{
            type: 'Numeric',
            position: 'left',
            fields: ['annualizedReturn'],
            label: {
                renderer: Ext.util.Format.numberRenderer('0,0.00')
            },
            title: 'Annualized Return',
            grid: true,
            minimum: -20.0,
            maximum: 100.0,
            constrain: false
        }, {
            type: 'Time',
            position: 'bottom',
            fields: ['date'],
            title: 'Date',
            dateFormat: 'n-j-y',
            step: [Ext.Date.MONTH, 3]
        }],
        series: [{
            type: 'line',
            highlight: {
                size: 7,
                radius: 7
            },
            axis: 'bottom',
            xField: 'date',
            yField: 'annualizedReturn',
            showMarkers: false,
            tips: {
                trackMouse: true,
                width: 200,
                height: 28,
                renderer: function(storeItem, item) {
                    this.setTitle(Ext.Date.format(storeItem.get('date'), 'n-j-y') + ': ' +
                        Ext.util.Format.numberRenderer('0.000')(storeItem.get('annualizedReturn'))+'%');
                }
            }
        }]
    });

    aggrReturnChart = Ext.create('Ext.chart.Chart', {
        height: 300,
        animate: true,
        store: Ext.create('Ext.data.Store', {
            model: 'TimeseriesValue',
            proxy: {
                type: 'memory'
            }
        }),
        axes: [{
            type: 'Numeric',
            position: 'left',
            fields: ['returnPct'],
            label: {
                renderer: Ext.util.Format.numberRenderer('0,0.00')
            },
            title: 'Return (%)',
            grid: true,
            constrain: false
        }, {
            type: 'Time',
            position: 'bottom',
            fields: ['date'],
            title: 'Date',
            dateFormat: 'n-j-y',
            step: [Ext.Date.MONTH, 3]
        }],
        series: [{
            type: 'line',
            highlight: {
                size: 7,
                radius: 7
            },
            axis: 'bottom',
            xField: 'date',
            yField: 'returnPct',
            showMarkers: false,
            tips: {
                trackMouse: true,
                width: 200,
                height: 28,
                renderer: function(storeItem, item) {
                    this.setTitle(Ext.Date.format(storeItem.get('date'), 'n-j-y') + ': ' +
                        Ext.util.Format.numberRenderer('0.000')(storeItem.get('returnPct'))+'%');
                }
            }
        }]
    });


    // Grids
    var perfGrid = Ext.create('Ext.grid.Panel', {
        title: 'Performance',
        store: aggrPerfStore,
        columns: [{
            text: 'Symbol', dataIndex: 'symbol', flex: 3,
                renderer: function(value, metaData, record) {
                    if(record.get('securityType') === 'OPT') {
                        return record.get('description');
                    }
                    return value;
                }
        },{
            text: 'Annualized Return', dataIndex: 'annualizedReturn', flex: 3,
                renderer: function(value) {
                    return Ext.util.Format.numberRenderer('0.000')((value - 1.0) * 100.0) + '%';
                }
        },{
            text: 'Return', dataIndex: 'return', flex: 2,
                renderer: function(value) {
                    return Ext.util.Format.numberRenderer('0.000')(value * 100.0) + '%';
                }
        },{
            text: 'Cost Basis', dataIndex: 'costBasis', flex: 2, renderer: Ext.util.Format.usMoney
        },{
            text: 'Profit Loss', dataIndex: 'profitLoss', flex: 2, renderer: Ext.util.Format.usMoney
        },{
            text: 'Term Length', dataIndex: 'termLength', flex: 2, hidden: true
        },{
            text: 'Open Date', dataIndex: 'openDate', flex: 2, xtype:'datecolumn', format:'n-j-y', hidden: true
        },{
            text: 'Close Date', dataIndex: 'closeDate', flex: 2, xtype:'datecolumn', format:'n-j-y', hidden: true
        }],
        selModel: {
            mode: 'SIMPLE'
        }
    });

    var aggrGrid = Ext.create('Ext.grid.Panel', {
        title: 'Aggregate Performance',
        store: taggedPerfStore,
        columns: [{
            text: 'Tag', dataIndex: 'tag', flex: 3
        },{
            text: 'Annualized Return', dataIndex: 'annualizedReturn', flex: 3,
                renderer: function(value, metaData, record) {
                    return Ext.util.Format.numberRenderer('0.000')((value - 1) * 100.0) + '%';
                }
        },{
            text: 'Return', dataIndex: 'return', flex: 2,
                renderer: function(value, metaData, record) {
                    return Ext.util.Format.numberRenderer('0.000')(value * 100.0) + '%';
                }
        },{
            text: 'Cost Basis', dataIndex: 'costBasis', flex: 2,
                renderer: Ext.util.Format.usMoney
        },{
            text: 'Profit Loss', dataIndex: 'profitLoss', flex: 2,
                renderer: Ext.util.Format.usMoney
        }]
    });


    // --- Listeners ---
    performanceStore.addListener('load', function() {
        performanceStore.group('cusip');
        var groups = performanceStore.getGroups();
        aggrPerfStore.loadData(groups.map(function(group) {
            return combinePositionPerformance(group.children, true);
        }), true);
        performanceStore.clearGrouping();
    });

    perfGrid.addListener('selectionchange', function(selModel, selected) {
        aggrGrid.getSelectionModel().deselectAll(true);
        if(selected.length > 0) {
            addTagged(selected, 'Selected');
        } else {
            var selectedRecord = taggedPerfStore.findRecord('tag', 'Selected');
            taggedPerfStore.remove(selectedRecord);
        }
    });

    aggrGrid.addListener('beforeselect', function(self, record) {
        lastSelectedTagName = record.get('tag');
        if(perfGrid.getStore() == performanceStore) {
            perfGrid.getSelectionModel().select(record.positions().getRange());
        } else {
            var selected = perfGrid.getSelectionModel().getSelection();
            perfGrid.getSelectionModel().deselect(selected, true);
            record.positions().each(function(parentRecord) {
                var matchRow = aggrPerfStore.findRecord('cusip', parentRecord.get('cusip'));
                perfGrid.getSelectionModel().select(matchRow, true, true);
            });
            perfGrid.fireEvent('selectionchange', null, record.positions().getRange());
        }

        // Update visualization
        var timeseriesData = record.aggregatePerformance.timeseriesData();
        timeseriesData.sort('date', 'DESC');
        var chartOriginRecord = timeseriesData.getAt(timeseriesData.findBy(function(cRecord) {
            return daysBetween(cRecord.get('date'), Ext.Date.parse(Ext.Date.now(), 'time')) > 365;
        })) || '';
        timeseriesData.filterBy(function(cRecord) {
            return daysBetween(cRecord.get('date'), Ext.Date.parse(Ext.Date.now(), 'time')) <= 365
        });
        timeseriesData.each(function(cRecord) {
            cRecord.set('annualizedReturn', chartOriginRecord);
            cRecord.set('returnPct', chartOriginRecord);
        });
        annualizedReturnChart.bindStore(timeseriesData);
        aggrReturnChart.bindStore(timeseriesData);
    });

    // --- Buttons ---
    btnDiv.add(Ext.create('Ext.button.Button', {
        text: 'Select None',
        handler: function() {
            perfGrid.getSelectionModel().deselectAll();
        }
    }));
    btnDiv.add(Ext.create('Ext.button.Button', {
        text: 'Combine/Expand Symbol',
        handler: function() {
            if(perfGrid.getStore() == aggrPerfStore) {
                perfGrid.reconfigure(performanceStore);

            } else {
                perfGrid.reconfigure(aggrPerfStore);
            }
        }
    }));
    btnDiv.add(Ext.create('Ext.button.Button', {
        text: 'Tag',
        handler: function() {
            Ext.Msg.prompt('Tag', 'Please enter your new tag:', function(btn, text){
                if (btn == 'ok'){
                    addTagged(perfGrid.getSelectionModel().getSelection(), text);
                }
            }, window, false, lastSelectedTagName);
        }
    }));
    btnDiv.add(Ext.create('Ext.button.Button', {
        text: 'Remove Tag',
        handler: function() {
            taggedPerfStore.remove(aggrGrid.getSelectionModel().getSelection());
        }
    }));

    bodyDiv.add(btnDiv);
    bodyDiv.add(viewDiv);
    viewDiv.add(perfGrid);
    rightCol.add(aggrGrid);
    rightCol.add(aggrReturnChart);
    rightCol.add(annualizedReturnChart);
    viewDiv.add(rightCol);
};

// --- Methods ---
var addTagged = function(selected, symbolText) {
    selected = selected.reduce(function(aggr, cVal) {
        if(cVal.parents) {
            aggr.push.apply(aggr, cVal.parents);
        } else {
            aggr.push(cVal);
        }
        return aggr;
    }, []);
    var newPerf = combinePositionPerformance(selected, symbolText);
    var recordToUpdate = taggedPerfStore.findRecord('tag', symbolText, 0, false, false, true);
    var newRecord = false;
    if(!recordToUpdate) {
        recordToUpdate = new PositionTag();
        newRecord = true;
    }
    recordToUpdate.set('tag', symbolText);
    recordToUpdate.aggregatePerformance = newPerf;
    recordToUpdate.set('annualizedReturn', newPerf.get('annualizedReturn'));
    recordToUpdate.set('return', newPerf.get('return'));
    recordToUpdate.set('profitLoss', newPerf.get('profitLoss'));
    recordToUpdate.set('costBasis', newPerf.get('costBasis'));
    recordToUpdate.positions().removeAll();
    recordToUpdate.positions().add(newPerf.parents);
    if(newRecord) {
        taggedPerfStore.add(recordToUpdate);
    } else {
        recordToUpdate.save();
    }
};

var combinePositionPerformance = function(positions, includeSymbol) {
    var newPositionPerf = new Position();
    newPositionPerf.parents = positions.slice(0);

    var validDays = [];
    positions.forEach(function(cPos) {
        cPos.quotes().each(function(cQuote) {
            if (!validDays.some(function(vDate) {
                return Ext.Date.isEqual(vDate, cQuote.get('date'));
            })) {
                validDays.push(cQuote.get('date'));
            }
        });
    });
    var hasHistoricalQuotes = validDays.length > 0;

    var costBasis = 0.0;
    var profitLoss = 0.0;
    var startTimestamp = null;
    var endTimestamp = null;
    positions.forEach(function(cSel) {
        costBasis += cSel.get('costBasis');
        profitLoss += cSel.get('profitLoss');
        var cStartDate = Ext.Date.format(cSel.get('openDate'), 'U');
        var cEndDate = Ext.Date.format(cSel.get('closeDate'), 'U');
        if (startTimestamp === null) {
            startTimestamp = cStartDate;
        } else {
            startTimestamp = Math.min(cStartDate, startTimestamp);
        }
        if (endTimestamp === null) {
            endTimestamp = cEndDate;
        } else {
            endTimestamp = Math.max(cEndDate, endTimestamp);
        }
        if (!validDays.some(function(vDate) {
            return Ext.Date.isEqual(vDate, cSel.get('closeDate'));
        })) {
            validDays.push(cSel.get('closeDate'));
        }
    });
    newPositionPerf.set('costBasis', costBasis);
    newPositionPerf.set('profitLoss', profitLoss);
    newPositionPerf.set('return', profitLoss/costBasis);
    // TODO this variable is poor form can be boolean or string
    if(includeSymbol === true) {
        newPositionPerf.set('symbol', positions[0].get('symbol'));
        newPositionPerf.set('cusip', positions[0].get('cusip'));
        newPositionPerf.set('description', positions[0].get('description'));
        newPositionPerf.set('securityType', positions[0].get('securityType'));
    } else {
        newPositionPerf.set('symbol', includeSymbol);
    }

    var today = Ext.Date.parse(Ext.Date.now(), 'time');
    Ext.Date.clearTime(today);

    var compoundReturnRate = 1.0;
    var cDate = Ext.Date.parse(startTimestamp, 'U');
    var startDate = cDate;
    var startCostBasis = null;
    var endDate = Ext.Date.parse(endTimestamp, 'U');
    var lastDate = null;
    var lastAggrGain = 0;
    while (startTimestamp !== null && Ext.Date.format(cDate, 'time') <= Ext.Date.format(today, 'time')) {
        if(hasHistoricalQuotes && !validDays.some(function(vDate) {
            return Ext.Date.isEqual(vDate, cDate);
        })) {
            cDate = Ext.Date.add(cDate, Ext.Date.DAY, 1);
            continue;
        }

        var aggrValue = 0.0;
        var aggrCostBasis = 0.0;
        positions.forEach(function(cSel) {
            if(!Ext.Date.between(cDate, cSel.get('openDate'), cSel.get('closeDate'))) {
                return;
            }

            var quotes = cSel.quotes();
            if(quotes.count() > 0) {
                if(Ext.Date.isEqual(cSel.get('openDate'), cDate)) {
                    aggrCostBasis += cSel.get('costBasis');
                } else {
                    var quoteIdx = quotes.findBy(findFn(lastDate));
                    if(quoteIdx === -1) {
                        console.log("Error no quote found for last date " + cSel.get('symbol') + " on " + lastDate);
                        return;
                    }
                    var pQuote = quotes.getAt(quoteIdx);
                    aggrCostBasis += pQuote.get('close') * cSel.get('shares');
                }
                if(Ext.Date.isEqual(cSel.get('closeDate'), cDate)) {
                    aggrValue += cSel.get('closeValue');
                } else {
                    var quoteIdx = quotes.findBy(findFn(cDate));
                    if(quoteIdx === -1) {
                        console.log("Error no quote found for current date " + cSel.get('symbol') + " on " + cDate);
                        return;
                    }
                    var cQuote = quotes.getAt(quoteIdx);
                    aggrValue += cQuote.get('close') * cSel.get('shares');
                }
            } else {
                // Old way not using timeseries data for options
                var priorTerm = daysBetween(cSel.get('openDate'), lastDate) + 1;
                var currTerm = daysBetween(lastDate, cDate);
                if (Ext.Date.isEqual(cSel.get('openDate'), cDate)) {
                    priorTerm = 0;
                    currTerm = 1;
                }
                var adjCostBasis = cSel.get('costBasis') * Math.pow(cSel.get('annualizedReturn'), priorTerm/365.0);
                aggrCostBasis += adjCostBasis;
                aggrValue += adjCostBasis * Math.pow(cSel.get('annualizedReturn'), currTerm/365.0);
            }
        });
        if(aggrCostBasis > 0) {
            if(startCostBasis === null) {
                startCostBasis = aggrCostBasis;
            }
            compoundReturnRate *= aggrValue/aggrCostBasis;
            var aggrGain = lastAggrGain + aggrValue - aggrCostBasis;
            newPositionPerf.timeseriesData().add({
                'date': cDate,
                'startDate': startDate,
                'closeValue': aggrValue,
                'aggrGain': aggrGain,
                'returnRate': compoundReturnRate
            });
            lastAggrGain = aggrGain;
        }
        lastDate = cDate;
        cDate = Ext.Date.add(cDate, Ext.Date.DAY, 1);
    };
    var totalTerm = 1 + daysBetween(startDate, endDate);
    newPositionPerf.set('annualizedReturn', Math.pow(compoundReturnRate, 365.0/totalTerm));
    return newPositionPerf;
};

var findFn = function(match) {
    return function(currQuote) {
        return Ext.Date.isEqual(currQuote.get('date'), match);
    };
};

var daysBetween = function(startDate, endDate) {
    return Math.round((endDate - startDate) / 86400000.0);
};

var createPositionStore = function() {
    var store = Ext.create('Ext.data.Store', {
        model: 'Position',
        sorters: [{
            property: 'annualizedReturn',
            direction: 'DESC'
        }]
    });
    store.on('load', function() {
       taggedPerfStore.load();
    });
    store.load();
    return store;
};

var createAggrPerfStore = function() {
    return Ext.create('Ext.data.Store', {
        model: 'Position',
        proxy: {
            type: 'memory'
        },
        sorters: [{
            property: 'annualizedReturn',
            direction: 'DESC'
        }]
    });
};

var createTagStore = function() {
    var store = Ext.create('Ext.data.Store', {
        model: 'PositionTag',
        autoSync: true,
        sorters: [{
            property: 'annualizedReturn',
            direction: 'DESC'
        }]
    });
    store.addListener('load', function() {
        addTagged(performanceStore.getRange(), 'All');
    });
    return store;
};

var createBookkeepingStore = function() {
    var store = Ext.create('Ext.data.Store', {
        model: 'Bookkeeping',
        autoLoad: true
    });
    store.addListener('load', function() {
        promptUnresolvedBookkeeping();
    });
    return store;
};
var defineOverrides = function() {
    Ext.override(Ext.data.reader.Json, {
        read: function(response) {
            var resultSet = this.callParent([response]);
            if(resultSet.count > 0) {
                resultSet.records.forEach(function(record) {
                    if(record.$className === "PositionTag") {
                        var positionStore = record.positions();
                        var positions = positionStore.getRange().map(function(position) {
                            var id = position.getId();
                            return performanceStore.getById(id);
                        });
                        positionStore.removeAll();
                        positionStore.add(positions);
                        record.aggregatePerformance =
                            combinePositionPerformance(positions, record.get('tag'));
                        record.set('annualizedReturn', record.aggregatePerformance.get('annualizedReturn'));
                        record.set('return', record.aggregatePerformance.get('return'));
                        record.set('profitLoss', record.aggregatePerformance.get('profitLoss'));
                        record.set('costBasis', record.aggregatePerformance.get('costBasis'));
                    }
                });
            }
            return resultSet;
        }
    });

    Ext.override(Ext.data.writer.Json, {
        getRecordData: function(record, operation) {
            var data = this.callParent([record, operation]);

            /* Iterate over all the hasMany associations */
            var assocLen = record.associations.getCount();
            for ( var i = 0; i < assocLen; i++) {
                var association = record.associations.getAt(i);
                if (association.type == 'hasMany' && association.persist !== false) {
                    data[association.name] = [];
                    var childStore = record[association.name]();

                    var processChild = function(childRecord) {
                        // Recursively get the record data for children (depth first)
                        var childData = this.getRecordData.call(this, childRecord, operation);
                        if (childRecord.dirty | childRecord.phantom | (childData != null)) {
                            data[association.name].push(childData);
                            record.setDirty();
                        }
                    };
                    childStore.each(processChild, this);
                } else if (association.type == 'hasOne' &&   association.persist !== false) {
                    var childRecord = record[association.instanceName];
                    var childData = this.getRecordData.call(this, childRecord, operation);
                    if (childRecord.dirty | childRecord.phantom | (childData != null)) {
                        data[association.name] = childData;
                        record.setDirty();
                    }
                }
            }
            return data;
        }
    });
};

var promptUnresolvedBookkeeping = function() {
    bookkeepingStore.each(function(record) {
        if(record.getBookkeepingResolution().get('type') === 'U' && bookkeepingWindow.isHidden()) {
            bookkeepingForm.loadRecord(record);
            bookkeepingWindow.show();
        }
    });
};

var createBookkeepingWindow = function() {
    bookkeepingWindow = Ext.create('Ext.window.Window');
    bookkeepingForm = Ext.create('Ext.form.Panel');

    bookkeepingForm.add(Ext.create('Ext.form.field.Display', {
        fieldLabel: 'Symbol',
        name: 'symbol'
    }));
    bookkeepingForm.add(Ext.create('Ext.form.field.Display', {
        fieldLabel: 'Date',
        name: 'date'
    }));
    bookkeepingForm.add(Ext.create('Ext.form.field.Display', {
        fieldLabel: 'Cusip',
        name: 'cusip'
    }));
    bookkeepingForm.add(Ext.create('Ext.form.field.Display', {
        fieldLabel: 'Description',
        name: 'description'
    }));
    bookkeepingForm.add(Ext.create('Ext.form.field.Display', {
        fieldLabel: 'Amount',
        name: 'amount'
    }));
    bookkeepingForm.add(Ext.create('Ext.form.field.Display', {
        fieldLabel: 'Quantity',
        name: 'quantity'
    }));

    resolutionForm = Ext.create('Ext.form.Panel');
    resolutionForm.add(Ext.create('Ext.form.field.ComboBox', {
        fieldLabel: 'Parent Cusip',
        name: 'parentCusip',
        allowBlank: false,
        queryMode: 'local',
        displayField: 'description',
        valueField: 'cusip',
        store: performanceStore
    }));

    resolutionForm.add(Ext.create('Ext.form.field.Number', {
        fieldLabel: 'Split Ratio',
        name: 'splitRatio',
        allowBlank: false
    }));

    bookkeepingWindow.add(bookkeepingForm);
    bookkeepingWindow.add(resolutionForm);

    bookkeepingWindow.addDocked(Ext.create('Ext.toolbar.Toolbar', {
        dock: 'bottom',
        items: [ '->',
            Ext.create('Ext.button.Button', {
                text: 'Cancel',
                handler: function() { bookkeepingWindow.close(); }
            }),
            Ext.create('Ext.button.Button', {
                text: 'Resolve',
                handler: function() {
                    var cRecord = bookkeepingForm.getRecord()
                    resolutionForm.getForm().updateRecord(cRecord.getBookkeepingResolution());
                    cRecord.getBookkeepingResolution().set('type', 'S');
                    cRecord.save({
                        success: function() {
                            location.reload();
                        }
                    });
                    bookkeepingWindow.close();
                }
            })
        ]
    }));
};
