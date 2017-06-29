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

    PlagCheckController.$inject = ['PlagCheckService', 'AlertService'];

    function PlagCheckController(PlagCheckService, AlertService) {
        var vm = this;
        vm.checkForPlagiarism = checkForPlagiarism;
        vm.results = {};
        /**
         * 'plagCheckDoc', 'checkUnpublishedWork'
         */
        vm.data = {};

        function checkForPlagiarism() {
            PlagCheckService.checkForPlagiarism(vm.data).then(function(response) {
                if(response.success) {
                    vm.results = angular.fromJson(response.resultJsonString);
                    vm.data.success = response.success;
                } else {
                    if(response.error)
                        vm.data.error = response.error;
                    else
                        vm.data.error = response;
                    AlertService.error(vm.data.error);
                }
            });
        }
    }
})();
