var topologyStabilizationGraph = null;

$(document).ready(function () {

    topologyStabilizationGraph = Morris.Line({
        element: 'topology_stabilization_graph',
        data: [
            { y: '2012-02-24 15:00:00', a: 100 },
            { y: '2012-02-24 15:01:00', a: 75 },
            { y: '2012-02-24 15:02:00', a: 50 },
            { y: '2012-02-24 15:03:00', a: 75 },
            { y: '2012-02-24 15:04:00', a: 50 },
            { y: '2012-02-24 15:05:00', a: 75 },
            { y: '2012-02-24 15:06:00', a: 100 }
        ],
        xkey: 'y',
        ykeys: ['a'],
        ymin: 0,
        ymax: 100,
        labels: ['Series A']
    });

    updateStatistics();
    topologyStabilizationInterval = window.setInterval(function () {
        updateTopologyStabilizationStatistics();
    }, 10000);
});

var updateTopologyStabilizationStatistics = function () {
    $.ajax({
        url: "/webfrontend/getTopologyStabilizationStatistics"
    })
        .done(function (data) {
            topologyStabilizationGraph.setData(data);
        })
        .fail(function(){
            console.log("Could not reach pathFinder backend");

            clearInterval(topologyStabilizationInterval);
        })
};