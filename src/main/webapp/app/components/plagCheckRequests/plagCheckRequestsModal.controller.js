/**
 * Created by Jagrut on 05-07-2017.
 * Handle functions related to modals inside the plagiarism check requests page.
 */
(function() {
    'use strict';

    angular
        .module('plagUiApp')
        .controller('PlagCheckRequestsModalController', PlagCheckRequestsModalController);

    PlagCheckRequestsModalController.$inject = ['$uibModalInstance', 'PlagCheckRequestsService', 'AlertService', 'requestDetails', '$state'];

    function PlagCheckRequestsModalController($uibModalInstance, PlagCheckRequestsService, AlertService, requestDetails, $state) {
        var vm = this;
        vm.sendAuthorDocument = sendAuthorDocument;
        vm.sendUserDocument = sendUserDocument;
        vm.cancel = cancel;
        vm.isSameFile = isSameFile;
        vm.acceptModalFormData = {};
        vm.userDocModalFormData = {};
        vm.requestDetails = requestDetails;
        vm.sameFile = true;

        /**
         * Close the modal without performing the operation it was intended for.
         */
        function cancel() {
            vm.acceptModalFormData = {};
            $uibModalInstance.dismiss('cancel');
        }

        /**
         * Send the accept request author file again to the backend and handle call backs.
         */
        function sendAuthorDocument() {
            PlagCheckRequestsService.acceptRequestWithDoc(vm.requestDetails, vm.acceptModalFormData.acceptRequestDoc).then(function(response) {
                if(response.success) {
                    $uibModalInstance.close();
                    $state.reload();
                } else if(response.error)
                    AlertService.error(response.error);
                else
                    AlertService.error(response);
            });
        }

        /**
         * Send the accept request user file to the backend and handle call backs.
         */
        function sendUserDocument() {
            PlagCheckRequestsService.userDocRequest(vm.requestDetails, vm.userDocModalFormData.userDocRequest).then(function(response) {
                if(response.success) {
                    console.log(response);
                    $uibModalInstance.close();
                } else if(response.error)
                    AlertService.error(response.error);
                else
                    AlertService.error(response);
            });
        }

        /**
         * Check if the file selected is same file as specified in Request.
         */
        function isSameFile(requestFileName, selectedFileName) {
            vm.sameFile = requestFileName === selectedFileName;
        }
    }
})();
