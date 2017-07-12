var topologyPullInterval = null;

$(document).ready(function () {
    showFallbackTopology();
    updateTopology();
    topologyPullInterval = window.setInterval(function () {
        updateTopology();
    }, 10000);
});


var showFallbackTopology = function () {
    $("#topology_content_fallback").show();
    $("#topology_content").hide();
}

var updateTopology = function () {

    $.ajax({
        url: "/webfrontend/getTopology"
    })
        .done(function (data) {
            if (totalVispInstances === 0) {
                // topology is empty
                showFallbackTopology();
                return;
            } else {
                var topologyFileContent = (window.atob(data.topology)).replace(/(?: )/g, '&nbsp;').replace(/(?:\r\n|\r|\n)/g, '<br />');

                $("#topology_file_content").html(topologyFileContent);
                $("#topology_content_fallback").hide();
                $("#topology_content").show();

            }

        })
        .fail(function () {
            console.log("Could not reach pathFinder backend");
            showFallbackTopology();
            clearInterval(topologyPullInterval);
        });
};

