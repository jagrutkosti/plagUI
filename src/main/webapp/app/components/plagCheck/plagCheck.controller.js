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

    PlagCheckController.$inject = ['$scope', 'PlagCheckService', 'AlertService', 'vcRecaptchaService', 'currentUserWalletAddress', 'pdServers'];

    function PlagCheckController($scope, PlagCheckService, AlertService, vcRecaptchaService, currentUserWalletAddress, pdServers) {
        var vm = this;
        vm.checkForPlagiarism = checkForPlagiarism;
        vm.createPlagCheckRequest = createPlagCheckRequest;
        vm.setResponse = setResponse;
        vm.setWidgetId = setWidgetId;
        vm.cbExpiration = cbExpiration;
        vm.setStreamNames = setStreamNames;
        vm.currentUserWalletAddress = currentUserWalletAddress;
        vm.results = {};
        vm.isEmpty = null;
        vm.pdServers = pdServers;
        vm.plagCheckDocFileName = '';
        vm.recaptcha = {
            key: '6LfGcycUAAAAAAn3Aanri79ijQSwust7kH_BH9Bd',
            response: null,
            widgetId: null
        };
        /**
         * 'plagCheckDoc', 'checkUnpublishedWork'
         */
        vm.data = {
            streamNames : []
        };

        /**
         * Calls the service function to check for plagiarism of the uploaded document and handles the response.
         */
        function checkForPlagiarism() {
            PlagCheckService.checkForPlagiarism(vm.data, vm.recaptcha.response).then(function(response) {
                if(response.success) {
                    vm.results = angular.fromJson(response.resultJsonString);
                    for(var key in vm.results) {
                        if(vm.results.hasOwnProperty(key)) {
                            vm.results[key] = angular.fromJson(vm.results[key]);
                        }
                    }
                    if(Object.keys(vm.results).length === 0 && vm.results.constructor === Object)
                        vm.isEmpty = true;
                    else
                        vm.isEmpty = false;
                    vm.plagCheckDocFileName = response.plagCheckDocFileName;
                } else {
                    vm.data = {};
                    if(response.error)
                        vm.data.error = response.error;
                    else
                        vm.data.error = response;
                    AlertService.error(vm.data.error);
                }
                vm.data = {};
                $scope.pdfPlagCheck.$setPristine();
                vm.cbExpiration();
            });
        }

        /**
         * Create a request for the author to view by calling the backend from service
         * @param simDocDetails the details of the document for which the request needs to be created
         */
        function createPlagCheckRequest(simDoc) {
            PlagCheckService.createPlagCheckRequest(simDoc, vm.plagCheckDocFileName).then(function(response) {
                if(response.success) {
                    simDoc.disabled = true;
                    AlertService.success("Request sent successfully!");
                } else {
                    vm.data = {};
                    if(response.error)
                        vm.data.error = response.error;
                    else
                        vm.data.error = response;
                    AlertService.error(vm.data.error);
                }
            })
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
            vm.data.streamNames = [];
            vm.pdServers.forEach(function (item) {
                if(item.selected)
                    vm.data.streamNames.push(item);
            })
        }
    }
})();
