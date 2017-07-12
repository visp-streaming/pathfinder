var operatorPullInterval = null;

$(document).ready(function () {
    showFallbackOperators();
    updateOperators();
    operatorPullInterval = window.setInterval(function () {
        updateOperators();
    }, 10000);
});


var showFallbackOperators = function () {
    $("#operators_content_fallback").show();
    $("#operators_content").hide();
}

var updateOperators = function () {

    $.ajax({
        url: "/webfrontend/getOperators"
    })
        .done(function (data) {
            if (totalVispInstances === 0 || data.operators.length === 0) {
                // topology is empty
                showFallbackOperators();
                return;
            } else {
                var htmlRowsOperators = "";

                var labelWorking = "<span class=\"label label-success\">working</span>";
                var labelFailed = "<span class=\"label label-danger\">failed</span>";

                $.each(data.operators, function (index, value) {
                    var label = "";
                    if(value.operatorStatus == "working") {
                        label = labelWorking;
                    } else {
                        label = labelFailed;
                    }
                    var newItem = "<tr>" +
                        "                                    <th scope=\"row\">" + value.id + "</th>" +
                        "                                    <td>" + value.concreteLocation + "</td>" +
                        "                                    <td><span class=\"label label-default\">" + value.subclass + "</span></td>" +
                        "                                    <td>" + label + "</td>" +
                        "                                    <td><button type=\"button\" class=\"btn " +
                        "btn-xs btn-primary\" onclick='showOperatorInfo(\"" + value.id + "\")'>Info</button></td>\n" +
                        "                                </tr>";
                    htmlRowsOperators += newItem;
                });

                $("#operators_rows").html(htmlRowsOperators);


                var htmlRowsSplitOperators = "";

                $.each(data.splitOperators, function (index, value) {

                    var newItem = "<tr>" +
                        "                                    <th scope=\"row\">" + value.id + "</th>" +
                        "                                    <td>" + value.activePath + "</td>" +
                        "                                    <td>" + value.totalPaths + "</td>" +
                        "                                    <td>" + value.failedPaths + "</td>" +
                        "                                    <td>" + value.availablePaths + "</td>" +
                        "                                    <td><button type=\"button\" class=\"btn " +
                        "btn-xs btn-primary\" onclick='showSplitOperatorInfo(\"" + value.id + "\")'>Info</button></td>\n" +
                        "                                </tr>";
                    htmlRowsSplitOperators += newItem;
                });

                $("#split_operators_rows").html(htmlRowsSplitOperators);


                $("#operators_content_fallback").hide();
                $("#operators_content").show();
            }

        })
        .fail(function () {
            console.log("Could not reach pathFinder backend");
            showFallbackOperators();
            clearInterval(operatorPullInterval);
        });
};

