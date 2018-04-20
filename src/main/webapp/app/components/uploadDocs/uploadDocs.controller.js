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

    UploadDocsController.$inject = ['UploadDocsService', 'AlertService', '$state', 'vcRecaptchaService', 'streamPermissionsAndRequests', 'pdServers'];

    function UploadDocsController(UploadDocsService, AlertService, $state, vcRecaptchaService, streamPermissionsAndRequests, pdServers){
        var vm = this;
        vm.uploadDocForBlockchain = uploadDocForBlockchain;
        vm.uploadTextForBlockchain = uploadTextForBlockchain;
        vm.uploadImageForBlockchain = uploadImageForBlockchain;
        vm.setResponse = setResponse;
        vm.setWidgetId = setWidgetId;
        vm.cbExpiration = cbExpiration;

        vm.pdServers = pdServers;

        /**
         * fileData: {'fileToHash', 'textToHash', 'imageToHash', 'streamName', 'contactInfo', 'fileName', 'success', 'error'}
         */
        vm.fileData = {
            streamNames : []
        };
        vm.streamPermissions = streamPermissionsAndRequests;
        vm.recaptcha = {
            key: '6LfGcycUAAAAAAn3Aanri79ijQSwust7kH_BH9Bd',
            response: null,
            widgetId: null
        };

        /**
         * Redirect the request to angular service after receiving the file from UI.
         * Handle the response from the server and redirect accordingly.
         */
        function uploadDocForBlockchain() {
            setStreamNames();
            UploadDocsService.uploadDocForBlockchain(vm.fileData, vm.recaptcha.response).then(function(response){
                if(response.success) {
                    vm.fileData.success = response.success;
                    $state.go('showDocDetails');
                } else {
                    vm.fileData = {};
                    if(response.error)
                        vm.fileData.error = response.error;
                    else
                        vm.fileData.error = response;
                    vm.cbExpiration();
                    AlertService.error(vm.fileData.error);
                }
            });
        }

        /**
         * Redirect the request to angular service after receiving the text from UI.
         * Handle the response from the server and redirect accordingly.
         */
        function uploadTextForBlockchain() {
            setStreamNames();
            UploadDocsService.uploadTextForBlockchain(vm.fileData, vm.recaptcha.response).then(function(response) {
                if(response.success) {
                    vm.fileData.success = response.success;
                    $state.go('showDocDetails');
                } else {
                    if(response.error)
                        vm.fileData.error = response.error;
                    else
                        vm.fileData.error = response;
                    vm.cbExpiration();
                    AlertService.error(vm.fileData.error);
                }
            });
        }

        /**
         * Redirect the request to angular service after receiving the image from UI.
         * Handle the response from the server and redirect accordingly.
         */
        function uploadImageForBlockchain() {
            UploadDocsService.uploadImageForBlockchain(vm.fileData, vm.recaptcha.response).then(function(response) {
                if(response.success) {
                    vm.fileData.success = response.success;
                    $state.go('showDocDetails');
                } else {
                    if(response.error)
                        vm.fileData.error = response.error;
                    else
                        vm.fileData.error = response;
                    vm.cbExpiration();
                    AlertService.error(vm.fileData.error);
                }
            });
        }

        /**
         * Setter for Google recaptcha response
         * @param response user response for recaptcha
         */
        function setResponse(response) {
            vm.recaptcha.response = response;
        }

        /**
         * Setter for widget
         * @param widgetId widgetId of one recaptcha entity
         */
        function setWidgetId(widgetId) {
            vm.recaptcha.setWidgetId = widgetId;
        }

        /**
         * Reload the Google recaptcha in case of bad/false response
         */
        function cbExpiration() {
            vcRecaptchaService.reload(vm.recaptcha.widgetId);
            vm.recaptcha.response = null;
        }

        function setStreamNames() {
            vm.pdServers.forEach(function (item) {
                if(item.selected)
                    vm.fileData.streamNames.push(item);
            })
        }
    }
})();
