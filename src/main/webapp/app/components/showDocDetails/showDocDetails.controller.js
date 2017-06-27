/**
 * Created by Jagrut on 07-06-2017.
 */
(function(){
    "use strict";
    /**
     * Controller to handle all events for showing document details for current user
     */
    angular
        .module('plagUiApp')
        .controller('DocDetailsController', DocDetailsController);

    DocDetailsController.$inject = ['DocDetailsService', 'AlertService', 'docDetails'];

    function DocDetailsController(DocDetailsService, AlertService, docDetails){
        var vm = this;
        vm.docDetails = angular.toJson(docDetails);
    }
})();
