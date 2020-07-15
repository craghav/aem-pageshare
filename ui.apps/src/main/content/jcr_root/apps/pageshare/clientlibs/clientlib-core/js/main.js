(function(window, $) {
    "use strict";



    $(window).adaptTo("foundation-registry").register("foundation.collection.action.action", {
        name: "cq-admin.pageshare.action.create",
        handler: function(name, el, config, collection, selections) {
            var url = config.data.href;
            window.location = url;
        }
    });
})(window, Granite.$);
