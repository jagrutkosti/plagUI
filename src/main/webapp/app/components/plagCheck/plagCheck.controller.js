/**
 * Created by Jagrut on 21-06-2017.
 */
(function() {
    "use strict";
    /**
     * Controller to handle all events related to checking a document for plagiarism
     * and showing results.
     */
    angular
        .module('plagUiApp')
        .controller('PlagCheckController', PlagCheckController);

    PlagCheckController.$inject = ['PlagCheckService', 'AlertService', 'vcRecaptchaService'];

    function PlagCheckController(PlagCheckService, AlertService, vcRecaptchaService) {
        var vm = this;
        vm.checkForPlagiarism = checkForPlagiarism;
        vm.setResponse = setResponse;
        vm.setWidgetId = setWidgetId;
        vm.cbExpiration = cbExpiration;
        vm.results = {};
        vm.recaptcha = {
            key: '6LfGcycUAAAAAAn3Aanri79ijQSwust7kH_BH9Bd',
            response: null,
            widgetId: null
        };
        /**
         * 'plagCheckDoc', 'checkUnpublishedWork'
         */
        vm.data = {};

        /**
         * Calls the service function to check for plagiarism of the uploaded document and handles the response.
         */
        function checkForPlagiarism() {
            PlagCheckService.checkForPlagiarism(vm.data, vm.recaptcha.response).then(function(response) {
                if(response.success) {
                    vm.results = angular.fromJson(response.resultJsonString);
                    vm.data.success = response.success;
                    console.log(vm.results);
                } else {
                    vm.data = {};
                    if(response.error)
                        vm.data.error = response.error;
                    else
                        vm.data.error = response;
                    AlertService.error(vm.data.error);
                }
                vcRecaptchaService.reload(vm.recaptcha.widgetId);
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
    }
})();
