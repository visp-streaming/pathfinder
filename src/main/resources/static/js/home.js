var pullInterval = null;

$(document).ready(function () {

    updateStatistics();
    pullInterval = window.setInterval(function () {
        updateStatistics();
    }, 10000);
});

var updateStatistics = function () {

    $.ajax({
        url: "/webfrontend/getStatistics"
    })
        .done(function (data) {
            $('#statistics_ip').text(data['ip']);
            $('#statistics_port').text(data['port']);
            $('#statistics_instances').text(data['instances']);
            $('#statistics_version').text(data['version']);
            $('#statistics_dbentries').text(data['dbentries']);
            $('#statistics_uptime').text(data['uptime']);
        })
        .fail(function(){
            console.log("Could not reach pathFinder backend");
            $('#statistics_ip').text("-");
            $('#statistics_port').text("-");
            $('#statistics_instances').text("-");
            $('#statistics_version').text("-");
            $('#statistics_dbentries').text("-");
            $('#statistics_uptime').text("OFFLINE");
            clearInterval(pullInterval);
        })
};