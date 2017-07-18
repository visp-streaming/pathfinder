var topologyStabilizationGraph = null;
var topologyStabilizationInterval = null;

$(document).ready(function () {

    topologyStabilizationGraph = Morris.Line({
        element: 'topology_stabilization_graph',
        data: [
        ],
        xkey: 'y',
        ykeys: ['a'],
        ymin: 0,
        ymax: 1.0,
        smooth: false,
        hideHover: 'always',
        labels: ['Series A']
    });

    updateStatistics();
    topologyStabilizationInterval = window.setInterval(function () {
        updateTopologyStabilizationStatistics();
    }, GLOBAL_POLL_INTERVAL);
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