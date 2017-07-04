/**
 * Created by Jagrut on 04-07-2017.
 */
(function() {
    "use strict";
    /**
     * Controller to handle all events related to plagiarism check requests sent/received for the user
     */
    angular
        .module('plagUiApp')
        .controller('PlagCheckRequestsController', PlagCheckRequestsController);

    PlagCheckRequestsController.$inject = ['PlagCheckRequestsService', 'AlertService', 'allRequests'];

    function PlagCheckRequestsController(PlagCheckRequestsService, AlertService, allRequests) {
        var vm = this;
        vm.allRequests = allRequests;
        vm.acceptRequest = acceptRequest;
        vm.rejectRequest = rejectRequest;

        function acceptRequest(request) {

        }

        /**
         * Calls the service method to reject the selected request and handles the call backs.
         * @param plagRequest the request to reject
         */
        function rejectRequest(plagRequest) {
            PlagCheckRequestsService.rejectRequest(plagRequest).then(function(response) {
                if(response.success) {
                    plagRequest.status = 2;
                    AlertService.success(response.success);
                } else if(response.error)
                    AlertService.error(response.error);
                else
                    AlertService.error(response);
            });
        }
    }
})();
