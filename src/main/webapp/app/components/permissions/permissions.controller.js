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

    PermissionsController.$inject = ['$state', 'PermissionsService', 'AlertService', 'streamPermissionsAndRequests', 'currentUserWalletAddress'];

    function PermissionsController($state, PermissionsService, AlertService, streamPermissionsAndRequests, currentUserWalletAddress) {
        var vm = this;
        vm.streamPermissionsAndRequests = streamPermissionsAndRequests;
        vm.requestPermission = requestPermission;
        vm.grantPermission = grantPermission;
        vm.rejectPermission = rejectPermission;
        vm.currentUserWalletAddress = currentUserWalletAddress;
        vm.hasUserResponded = hasUserResponded;

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
            PermissionsService.grantPermission(streamRequest).then(function(response) {
                if(response.success) {
                    if(stream.writeRequestStatus === 1)
                        stream.writeRequestStatus = 2;
                    if(stream.adminRequestStatus === 1)
                        stream.adminRequestStatus = 2;
                    $state.reload();
                    AlertService.success('Request granted successfully!');
                }
                else if(response.error)
                    AlertService.error(response.error);
                else
                    AlertService.error(response);
            })
        }

        /**
         * Rejects permission for this logged in admin.
         * @param streamRequest the permission stream request to reject.
         */
        function rejectPermission(streamRequest) {
            PermissionsService.rejectPermission(streamRequest).then(function(response) {
                if(response.success) {
                    if(streamRequest.writeRequestStatus === 1)
                        streamRequest.writeRequestStatus = 3;
                    if(streamRequest.adminRequestStatus === 1)
                        streamRequest.adminRequestStatus = 3;
                    $state.reload();
                    AlertService.success('Request rejected successfully!');
                }
                else if(response.error)
                    AlertService.error(response.error);
                else
                    AlertService.error(response);
            })
        }

        /**
         * Utility method to check if the user has responded or not.
         * @param checkList the list to check
         * @returns {boolean}
         */
        function hasUserResponded(checkList) {
            return checkList.indexOf(vm.currentUserWalletAddress) !== -1;
        }
    }
})();
