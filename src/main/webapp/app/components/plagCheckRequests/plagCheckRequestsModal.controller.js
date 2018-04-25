/**
 * Created by Jagrut on 05-07-2017.
 * Handle functions related to modals inside the plagiarism check requests page.
 */
(function() {
    'use strict';

    angular
        .module('plagUiApp')
        .controller('PlagCheckRequestsModalController', PlagCheckRequestsModalController);

    PlagCheckRequestsModalController.$inject = ['$uibModalInstance', 'PlagCheckRequestsService', 'AlertService', 'requestDetails', '$state', 'account'];

    function PlagCheckRequestsModalController($uibModalInstance, PlagCheckRequestsService, AlertService, requestDetails, $state, account) {
        var vm = this;
        vm.sendAuthorDocument = sendAuthorDocument;
        vm.sendUserDocument = sendUserDocument;
        vm.cancel = cancel;
        vm.isSameFile = isSameFile;
        vm.checkPassword = checkPassword;
        vm.account = account;
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
         * Checks if password is valid by decrypting the private key of the logged in user.
         */
        function checkPassword() {
            try {
                var bytes  = CryptoJS.AES.decrypt(vm.account.plagchainPrivkey, vm.userDocModalFormData.password);
                vm.userDocModalFormData.decryptedPrivKey = bytes.toString(CryptoJS.enc.Utf8);
            } catch(err) {
                vm.userDocModalFormData.decryptedPrivKey = '';
            } finally {
                vm.invalidPassword = vm.userDocModalFormData.decryptedPrivKey.length <= 5;
            }
        }

        /**
         * If priv key option is 1 i.e. user asked to store priv key in UI db as is, then we simply reassign it
         */
        function checkPrivKeyOption() {
            if(vm.account.privKeyOption === 1)
                vm.userDocModalFormData.decryptedPrivKey = vm.account.plagchainPrivkey;
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
            checkPrivKeyOption();
            PlagCheckRequestsService.userDocRequest(vm.requestDetails, vm.userDocModalFormData.userDocRequest, vm.userDocModalFormData.decryptedPrivKey).then(function(response) {
                if(response.success) {
                    $uibModalInstance.close(response);
                    $state.reload();
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
