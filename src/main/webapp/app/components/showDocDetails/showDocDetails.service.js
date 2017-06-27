/**
 * Created by Jagrut on 07-06-2017.
 */
(function(){
    "use strict";
    /**
     * Service method to send requests to backend for all document details related query.
     */
    angular
        .module('plagUiApp')
        .factory('DocDetailsService', DocDetailsService);

    DocDetailsService.$inject = ['$http'];

    function DocDetailsService($http) {
        const API_URL = '/api/plagchain/';
        var service = {};

        service.getDocDetails = function() {
            return $http.get(API_URL + 'getDocs').then(function(response) {
               return response.data;
            }, function(response) {
                if(response.status > 0) {
                    return response.status + ':' + response.statusText;
                }
            });
        };

        return service;
    }
})();
