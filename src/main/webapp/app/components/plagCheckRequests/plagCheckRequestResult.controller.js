/**
 * Created by Jagrut on 07-07-2017.
 * For displaying the result of the plagiarism check request
 */
(function() {
    'use strict';

    angular
        .module('plagUiApp')
        .controller('PlagCheckRequestResultController', PlagCheckRequestResultController);

    PlagCheckRequestResultController.$inject = ['$stateParams'];

    function PlagCheckRequestResultController($stateParams) {
        var vm = this;
        vm.userPlagCheckResult = angular.fromJson($stateParams.plagCheckResult);
    }
})();
