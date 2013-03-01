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