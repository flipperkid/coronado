Ext.Loader.setConfig({
    enabled: true,
    disableCaching: false
});
Ext.syncRequire('Ext.data.Request');
Ext.syncRequire('Ext.data.proxy.Rest');
Ext.syncRequire('Ext.data.writer.Json');
Ext.syncRequire('Ext.window.MessageBox');

Ext.define('Position', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'costBasis', type: 'decimal'},
        {name: 'closeValue', type: 'decimal'},
        {name: 'profitLoss', type: 'decimal', persist: false, convert: function(value, record) {
            return value || record.get('closeValue') - record.get('costBasis');
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
            return value || daysBetween(record.get('openDate'), record.get('closeDate')) + 1;
        }},
        {name: 'symbol', type: 'string'},
        {name: 'cusip', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'securityType', type: 'string'},
        {name: 'return', type: 'decimal', persist: false, convert: function(value, record) {
            return value || record.get('profitLoss')/record.get('costBasis');
        }},
        {name: 'annualizedReturn', type: 'decimal', persist: false, convert: function(value, record) {
            return value || Math.pow(1 + record.get('return'), 365/record.get('termLength'));
        }}
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
        {name: 'tag', type: 'string'}
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

Ext.onReady(function() {
    defineOverrides();

    //    loadTransactions();
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
        layout: 'column'
    });

    aggrPerfStore = createAggrPerfStore();
    taggedPerfStore = createTagStore();
    performanceStore = createPositionStore();

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
        width: '60%',
        selModel: {
            mode: 'SIMPLE'
        }
    });
    viewDiv.add(perfGrid);

    var aggrGrid = Ext.create('Ext.grid.Panel', {
        title: 'Aggregate Performance',
        store: taggedPerfStore,
        columns: [{
            text: 'Symbol', dataIndex: 'symbol', flex: 3,
                renderer: function(value, metaData, record) {
                    return record.aggregatePerformance.get('symbol');
                }
        },{
            text: 'Annualized Return', dataIndex: 'annualizedReturn', flex: 3,
                renderer: function(value, metaData, record) {
                    return Ext.util.Format.numberRenderer('0.000')(
                        (record.aggregatePerformance.get('annualizedReturn') - 1) * 100.0) + '%';
                }
        },{
            text: 'Return', dataIndex: 'return', flex: 2,
                renderer: function(value, metaData, record) {
                    return Ext.util.Format.numberRenderer('0.000')(
                        record.aggregatePerformance.get('return') * 100.0) + '%';
                }
        },{
            text: 'Cost Basis', dataIndex: 'costBasis', flex: 2,
                renderer: function(value, metaData, record) {
                    return Ext.util.Format.usMoney(record.aggregatePerformance.get('costBasis'));
                }
        },{
            text: 'Profit Loss', dataIndex: 'profitLoss', flex: 2,
                renderer: function(value, metaData, record) {
                    return Ext.util.Format.usMoney(record.aggregatePerformance.get('profitLoss'));
                }
        }],
        width: '40%'
    });
    viewDiv.add(aggrGrid);


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
        if(perfGrid.getStore() == performanceStore) {
            perfGrid.getSelectionModel().select(record.positions().getRange());
        } else {
            perfGrid.getSelectionModel().deselectAll(true);
            record.positions().each(function(parentRecord) {
                var matchRow = aggrPerfStore.findRecord('cusip', parentRecord.get('cusip'));
                perfGrid.getSelectionModel().select(matchRow, true, true);
            });
            perfGrid.fireEvent('selectionchange', null, record.positions().getRange());
        }
    });

    // --- Buttons ---
    btnDiv.add(Ext.create('Ext.button.Button', {
        text: 'Select All',
        handler: function() {
            perfGrid.getSelectionModel().selectAll();
        }
    }));
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
            });
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
    var recordToUpdate = taggedPerfStore.findRecord('tag', symbolText);
    var newRecord = false;
    if(!recordToUpdate) {
        recordToUpdate = new PositionTag();
        newRecord = true;
    }
    recordToUpdate.set('tag', symbolText);
    recordToUpdate.aggregatePerformance = newPerf;
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
    var costBasis = 0.0;
    var profitLoss = 0.0;
    var keyDates = [];
    positions.forEach(function(cSel) {
        costBasis += cSel.get('costBasis');
        profitLoss += cSel.get('profitLoss');
        keyDates.push(Ext.Date.format(cSel.get('openDate'), 'U'));
        keyDates.push(Ext.Date.format(Ext.Date.add(cSel.get('closeDate'), Ext.Date.DAY, 1), 'U'));
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

    var compoundReturnRate = 1.0;
    keyDates = Ext.Array.sort(Ext.Array.map(Ext.Array.unique(keyDates), function(val) {
        return Ext.Date.parse(val, 'U');
    }), function(val1, val2) {
        return val1 - val2;
    });
    keyDates.forEach(function(cDate, cIdx, cArr) {
        if(cIdx+1 === cArr.length) {
            return;
        }
        var cTerm = daysBetween(cDate, cArr[cIdx+1]);
        var aggrReturn = 0.0;
        var aggrCostBasis = 0.0;
        positions.forEach(function(cSel) {
            if(Ext.Date.between(cDate, cSel.get('openDate'), cSel.get('closeDate'))) {
                var priorTerm = daysBetween(cSel.get('openDate'), cDate);
                var adjCostBasis = cSel.get('costBasis') * Math.pow(cSel.get('annualizedReturn'), priorTerm/365.0);
                aggrCostBasis += adjCostBasis;
                aggrReturn += adjCostBasis * Math.pow(cSel.get('annualizedReturn'), cTerm/365.0);
//                    console.log(cSel.get('symbol') + ' ' + priorTerm + ' ' + adjCostBasis);
            }
        });
//            console.log(Ext.Date.format(cDate, 'n-j-y') + ' ' + ((Math.pow(aggrReturn/aggrCostBasis, 365.0/cTerm) - 1.0) * 100) +
//                ' ' + aggrCostBasis + ' ' + cTerm);
        if(aggrCostBasis > 0) {
            compoundReturnRate *= aggrReturn/aggrCostBasis;
        }
    });
    var totalTerm = daysBetween(keyDates[0], keyDates[keyDates.length-1]);
    newPositionPerf.set('annualizedReturn', Math.pow(compoundReturnRate, 365.0/totalTerm));
    return newPositionPerf;
};

var daysBetween = function(startDate, endDate) {
    return parseInt((endDate - startDate) / 86400000);
};

var createPositionStore = function() {
    var store = Ext.create('Ext.data.Store', {
        model: 'Position',
        sorters: [{
            property: 'annualizedReturn',
            direction: 'DESC'
        }]
    });
    store.on('datachanged', function() {
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
        autoSync: true
    });
    store.on('datachanged', function() {
        addTagged(performanceStore.getRange(), 'All');
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
                if (association.type == 'hasMany') {
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
                }
            }
            return data;
        }
    });
};