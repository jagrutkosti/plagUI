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

    PlagCheckRequestsController.$inject = ['PlagCheckRequestsService', 'AlertService', 'allRequests', '$uibModal', '$state', '$window', 'realTimeCurrencyBalance', 'account'];

    function PlagCheckRequestsController(PlagCheckRequestsService, AlertService, allRequests, $uibModal, $state, $window, realTimeCurrencyBalance, account) {
        var vm = this;
        vm.allRequests = allRequests;
        vm.acceptRequest = acceptRequest;
        vm.rejectRequest = rejectRequest;
        vm.userPlagCheck = userPlagCheck;
        vm.account = account;
        vm.account.realTimeCurrencyBalance = realTimeCurrencyBalance;
        var modalInstance = null;

        /**
         * Opens a modal dialog to allow user to upload the same file and send to backend
         * @param plagRequest the request to accept and corresponding document to upload
         */
        function acceptRequest(plagRequest) {
            if (modalInstance !== null) return;
            modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'app/components/plagCheckRequests/plagCheckRequestsAccept.html',
                controller: 'PlagCheckRequestsModalController',
                controllerAs: 'vm',
                resolve: {
                    requestDetails: plagRequest,
                    account: account
                }
            }).result.then(function() {
                plagRequest.status = 1;
                modalInstance = null;
            }, function() {
                modalInstance = null;
            });
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
                    $state.reload();
                } else if(response.error)
                    AlertService.error(response.error);
                else
                    AlertService.error(response);
            });
        }

        /**
         * Calls the service method to upload doc from user for the selected request and handles the call backs.
         * @param plagRequest the request to reject
         */
        function userPlagCheck(plagRequest) {
            if (modalInstance !== null) return;
            modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'app/components/plagCheckRequests/plagCheckRequestsUserDoc.html',
                controller: 'PlagCheckRequestsModalController',
                controllerAs: 'vm',
                resolve: {
                    requestDetails: plagRequest,
                    account: account
                }
            }).result.then(function(response) {
                plagRequest.status = 3;
                modalInstance = null;
                var url = $state.href('plagCheckRequestResult', {plagCheckResult: angular.toJson(response)});
                $window.open(url, '_blank');
            }, function() {
                modalInstance = null;
            });
        }
    }
})();
