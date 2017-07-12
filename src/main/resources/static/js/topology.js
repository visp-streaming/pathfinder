var topologyPullInterval = null;

$(document).ready(function () {

    updateTopology();
    topologyPullInterval = window.setInterval(function () {
        updateTopology();
    }, 10000);
});


var showFallback = function () {
    $("#topology_content_fallback").show();
    $("#topology_content").hide();
}

var updateTopology = function () {

    $.ajax({
        url: "/webfrontend/getTopology"
    })
        .done(function (data) {
            var topologyFileContent = (window.atob(data.topology)).replace(/(?: )/g, '&nbsp;').replace(/(?:\r\n|\r|\n)/g, '<br />');

            if (!(/\S/.test(topologyFileContent))) {
                // topology is empty
                showFallback();
                return;
            }
            $("#topology_file_content").html(topologyFileContent);
            $("#topology_content_fallback").hide();
            $("#topology_content").show();
        })
        .fail(function () {
            console.log("Could not reach pathFinder backend");
            showFallback();
            clearInterval(topologyPullInterval);
        });
};

