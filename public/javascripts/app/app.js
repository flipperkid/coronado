Ext.Loader.setConfig({
    enabled: true,
    disableCaching: false
});

Ext.define('Transaction', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'activity', type: 'string'},
        {name: 'amount', type: 'decimal'},
        {name: 'date', type: 'date', dateFormat: 'time'},
        {name: 'symbol', type: 'string'},
        {name: 'desc', type: 'string'},
        {name: 'cusip', type: 'string'}
    ]
});

Ext.define('Holding', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'costBasis', type: 'decimal'},
        {name: 'profitLoss', type: 'decimal'},
        {name: 'startDate', type: 'date', dateFormat: 'time' },
        {name: 'endDate', type: 'date', dateFormat: 'time' },
        {name: 'termLength', type: 'int', convert: function(value, record) {
            return value || daysBetween(record.get('startDate'), record.get('endDate')) + 1;
        }},
        {name: 'symbol', type: 'string'},
        {name: 'cusip', type: 'string'},
        {name: 'desc', type: 'string'},
        {name: 'type', type: 'string'},
        {name: 'return', type: 'decimal', convert: function(value, record) {
            return value || record.get('profitLoss')/record.get('costBasis');
        }},
        {name: 'annualizedReturn', type: 'decimal', convert: function(value, record) {
            return value || Math.pow(1 + record.get('return'), 365/record.get('termLength'));
        }}
    ]
});

Ext.onReady(function() {
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

    aggrPerfStore = Ext.create('Ext.data.Store', {
        model: 'Holding',
        proxy: {
            type: 'memory'
        },
        sorters: [{
            property: 'annualizedReturn',
            direction: 'DESC'
        }]
    });

    taggedPerfStore = Ext.create('Ext.data.Store', {
        model: 'Holding',
        proxy: {
            type: 'memory'
        },
        sorters: [{
            property: 'annualizedReturn',
            direction: 'DESC'
        }]
    });

    performanceStore = Ext.create('Ext.data.Store', {
        model: 'Holding',
        proxy: {
            type: 'ajax',
            url: '/performance',
            reader: {
                type: 'json'
            }
        },
        sorters: [{
            property: 'annualizedReturn',
            direction: 'DESC'
        }],
        autoLoad: true
    });

    performanceStore.addListener('load', function() {
        taggedPerfStore.remove(taggedPerfStore.findRecord('symbol', 'All'));
        addTagged(performanceStore.getRange(), 'All');
        performanceStore.group('cusip');
        var groups = performanceStore.getGroups();
        aggrPerfStore.loadData(groups.map(function(group) {
            return combinePositionPerformance(group.children, true);
        }), true);
        performanceStore.clearGrouping();
    });

    var perfGrid = Ext.create('Ext.grid.Panel', {
        title: 'Performance',
        store: aggrPerfStore,
        columns: [
            {
                text: 'Symbol', dataIndex: 'symbol', flex: 3,
                renderer: function(value, metaData, record) {
                    if(record.get('type') === 'OPT') {
                        return record.get('desc');
                    }
                    return value;
                }
            },
            {
                text: 'Annualized Return', dataIndex: 'annualizedReturn', flex: 3,
                renderer: function(value) {
                    return Ext.util.Format.numberRenderer('0.000')((value - 1.0) * 100.0) + '%';
                }
            },
            {
                text: 'Return', dataIndex: 'return', flex: 2,
                renderer: function(value) {
                    return Ext.util.Format.numberRenderer('0.000')(value * 100.0) + '%';
                }
            },
            { text: 'Cost Basis', dataIndex: 'costBasis', flex: 2, renderer: Ext.util.Format.usMoney },
            { text: 'Profit Loss', dataIndex: 'profitLoss', flex: 2, renderer: Ext.util.Format.usMoney },
            { text: 'Term Length', dataIndex: 'termLength', flex: 2, hidden: true },
            { text: 'Start Date', dataIndex: 'startDate', flex: 2, xtype:'datecolumn', format:'n-j-y', hidden: true },
            { text: 'End Date', dataIndex: 'endDate', flex: 2, xtype:'datecolumn', format:'n-j-y', hidden: true }
        ],
        width: '60%',
        selModel: {
            mode: 'SIMPLE'
        }
    });
    viewDiv.add(perfGrid);

    var aggrGrid = Ext.create('Ext.grid.Panel', {
        title: 'Aggregate Performance',
        store: taggedPerfStore,
        columns: [
            { text: 'Symbol', dataIndex: 'symbol', flex: 3 },
            {
                text: 'Annualized Return', dataIndex: 'annualizedReturn', flex: 3,
                renderer: function(value) {
                    return Ext.util.Format.numberRenderer('0.000')((value - 1) * 100.0) + '%';
                }
            },
            {
                text: 'Return', dataIndex: 'return', flex: 2,
                renderer: function(value) {
                    return Ext.util.Format.numberRenderer('0.000')(value * 100.0) + '%';
                }
            },
            { text: 'Cost Basis', dataIndex: 'costBasis', flex: 2, renderer: Ext.util.Format.usMoney },
            { text: 'Profit Loss', dataIndex: 'profitLoss', flex: 2, renderer: Ext.util.Format.usMoney }
        ],
        width: '40%'
    });
    viewDiv.add(aggrGrid);

    perfGrid.addListener('selectionchange', function(selModel, selected) {
        aggrGrid.getSelectionModel().deselectAll(true);
        taggedPerfStore.remove(taggedPerfStore.findRecord('symbol', 'Selected'));
        addTagged(selected, 'Selected');
    });

    aggrGrid.addListener('beforeselect', function(self, record) {
        if(perfGrid.getStore() == performanceStore) {
            perfGrid.getSelectionModel().select(record.parents);
        } else {
            perfGrid.getSelectionModel().deselectAll(true);
            record.parents.forEach(function(parentRecord) {
                var matchRow = aggrPerfStore.findRecord('cusip', parentRecord.get('cusip'));
                perfGrid.getSelectionModel().select(matchRow, true, true);
            });
            perfGrid.fireEvent('selectionchange', null, record.parents);
        }
    });

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

var loadTransactions = function() {
    var transactionStore = Ext.create('Ext.data.Store', {
        model: 'Transaction',
        groupField: 'activity',
        proxy: {
            type: 'ajax',
            url: '/transactions',
            reader: {
                type: 'json'
            }
        },
        sorters: [{
            property: 'date',
            direction: 'ASC'
        }],
        filters: [
            function(item) {
                return parseFloat(item.data.amount) !== 0 && item.data.symbol.length !== 0;
            }
        ],
        autoLoad: true
    });

    var transCountStore = Ext.create('Ext.data.Store', {
        fields: [{
            name: 'name'
        }, {
            name: 'total', convert: function(value, record){
                return record.raw.children.length;
            }
        }],
        data: transactionStore.getGroups()
    });

    var aggrValues = [];
    var transactionAggrStore = Ext.create('Ext.data.Store', {
        model: 'Transaction',
        proxy: {
            type: 'memory',
            data: aggrValues
        }
    });

    transactionStore.addListener('datachanged', function() {
        transCountStore.loadData(transactionStore.getGroups());
        aggrValues.length = 0;
        transactionStore.each(function(record) {
            var cDate = record.get('date');
            var cAmt = parseFloat(record.get('amount'));
            if(aggrValues.length > 0) {
                cAmt = cAmt + aggrValues[aggrValues.length-1].amount;
            }
//            console.log(Ext.Date.format(cDate, 'y-M-D') + '\t' + cAmt.toFixed(2) + '\t\t' + record.get('cusip') +
//                '\t\t' + record.get('activity') + '\n' + record.get('desc'));
            aggrValues.push({date: cDate, amount: cAmt});
        });
        transactionAggrStore.loadData(aggrValues);
    });

    Ext.create('Ext.chart.Chart', {
//        renderTo: Ext.getBody(),
        width: 1024,
        height: 300,
        animate: true,
        store: transactionAggrStore,
        axes: [{
            type: 'Numeric',
            position: 'left',
            fields: ['amount'],
            label: {
                renderer: Ext.util.Format.numberRenderer('0,0.00')
            },
            title: 'Amount',
            grid: true
        }, {
            type: 'Time',
            position: 'bottom',
            fields: ['date'],
            title: 'Date',
            dateFormat: 'n-j-y',
            step: [Ext.Date.MONTH, 6]
        }],
        series: [{
            type: 'line',
            highlight: {
                size: 7,
                radius: 7
            },
            axis: 'bottom',
            xField: 'date',
            yField: 'amount',
            markerConfig: {
                type: 'cross',
                size: 4,
                radius: 4,
                'stroke-width': 0
            }
        }]
    });

    var pieChart = Ext.create('Ext.chart.Chart', {
        width: 800,
        height: 600,
        animate: true,
        shadow: true,
        store: transCountStore,
        renderTo: Ext.getBody(),
        legend: {
            position: 'right'
        },
        insetPadding: 25,
        theme: 'Base:gradients',
        series: [{
            type: 'pie',
            field: 'total',
            showInLegend: true,
            highlight: {
                segment: {
                    margin: 20
                }
            },
            label: {
                field: 'name',
                display: 'rotate',
                contrast: true,
                font: '18px Arial'
            }
        }]
    });
};

var daysBetween = function(startDate, endDate) {
    return parseInt((endDate - startDate) / 86400000);
};

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
    taggedPerfStore.add(newPerf);
    return newPerf;
};

var combinePositionPerformance = function(positions, includeSymbol) {
    var newPositionPerf = new Holding();
    newPositionPerf.parents = positions.slice(0);
    var costBasis = 0.0;
    var profitLoss = 0.0;
    var keyDates = [];
    positions.forEach(function(cSel) {
        costBasis += cSel.get('costBasis');
        profitLoss += cSel.get('profitLoss');
        keyDates.push(Ext.Date.format(cSel.get('startDate'), 'U'));
        keyDates.push(Ext.Date.format(Ext.Date.add(cSel.get('endDate'), Ext.Date.DAY, 1), 'U'));
    });
    newPositionPerf.set('costBasis', costBasis);
    newPositionPerf.set('profitLoss', profitLoss);
    newPositionPerf.set('return', profitLoss/costBasis);
    if(includeSymbol === true) {
        newPositionPerf.set('symbol', positions[0].get('symbol'));
        newPositionPerf.set('cusip', positions[0].get('cusip'));
        newPositionPerf.set('desc', positions[0].get('desc'));
        newPositionPerf.set('type', positions[0].get('type'));
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
            if(Ext.Date.between(cDate, cSel.get('startDate'), cSel.get('endDate'))) {
                var priorTerm = daysBetween(cSel.get('startDate'), cDate);
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