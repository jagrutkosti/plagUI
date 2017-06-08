/**
 * Created by Jagrut on 07-06-2017.
 */
(function(){
    "use strict";
    /**
     * Controller to handle all events related to uploading a document or a text
     */
    angular
        .module('plagUiApp')
        .controller('UploadDocsController', UploadDocsController);

    UploadDocsController.$inject = ['UploadDocsService', 'AlertService'];

    function UploadDocsController(UploadDocsService, AlertService){
        var vm = this;
        /**
         * fileData: {'fileToHash', 'textToHash', 'ispublished', 'success', 'error'}
         */
        vm.fileData = {};
        vm.uploadDocForBlockchain = uploadDocForBlockchain;
        vm.uploadTextForBlockchain = uploadTextForBlockchain;

        /**
         * Redirect the request to angular service after receiving the file from UI.
         * Handle the response from the server and redirecting accordingly.
         * @param fileToHash the file from user to send to server
         */
        function uploadDocForBlockchain(fileToHash) {
            UploadDocsService.uploadDocForBlockchain(fileToHash).then(function(response){
                if(response.success) {
                    vm.fileData.success = response.success;
                } else {
                    vm.fileData.error = response;
                    AlertService.error(vm.fileData.error);
                }
            });
        }

        /**
         * Redirect the request to angular service after receiving the text from UI.
         * Handle the response from the server and redirecting accordingly.
         * @param textToHash the text from user to send to server
         */
        function uploadTextForBlockchain(textToHash) {
            UploadDocsService.uploadTextForBlockchain(textToHash).then(function(response) {
                if(response.success) {
                    vm.fileData.success = response.success;
                } else {
                    vm.fileData.error = response;
                    AlertService.error(vm.fileData.error);
                }
            });
        }
    }
})();
