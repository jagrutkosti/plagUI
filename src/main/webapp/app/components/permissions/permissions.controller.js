/**
 * Created by Jagrut on 11-07-2017.
 */
(function() {
    "use strict";
    /**
     * Controller to handle all events related to stream permission requests sent/received for the user
     */
    angular
        .module('plagUiApp')
        .controller('PermissionsController', PermissionsController);

    PermissionsController.$inject = ['PermissionsService', 'AlertService', 'streamPermissionsAndRequests'];

    function PermissionsController(PermissionsService, AlertService, streamPermissionsAndRequests) {
        var vm = this;
        vm.streamPermissionsAndRequests = streamPermissionsAndRequests;
        vm.requestPermission = requestPermission;
        vm.grantPermission = grantPermission;
        vm.rejectPermission = rejectPermission;

        /**
         * Creates a permission request for the user of the mentioned type and stream
         * @param stream the stream item for which the permission was requested
         * @param type permission type i.e. admin or write
         */
        function requestPermission(stream, type) {
            PermissionsService.requestPermission(stream, type).then(function(response) {
                if(response.success) {
                    if(type === 'write')
                        stream.writeRequestStatus = 1;
                    else if(type === 'admin')
                        stream.adminRequestStatus = 1;
                    AlertService.success('Request sent successfully!');
                }
                else if(response.error)
                    AlertService.error(response.error);
                else
                    AlertService.error(response);
            })
        }

        function grantPermission(streamRequest) {

        }

        function rejectPermission(streamRequest) {

        }
    }
})();
