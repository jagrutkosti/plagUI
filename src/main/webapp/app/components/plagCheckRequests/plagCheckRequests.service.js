/**
 * Created by Jagrut on 04-07-2017.
 */
(function() {
    "use strict";
    /**
     * Service method to send requests to backend for all events related to handling plagiarism check requests.
     */
    angular
        .module('plagUiApp')
        .factory('PlagCheckRequestsService', PlagCheckRequestsService);

    PlagCheckRequestsService.$inject = ['$http', 'Upload'];

    function PlagCheckRequestsService($http, Upload) {
        const API_URL = '/api/plagchain/';
        var service = {};

        /**
         * Call PlagCheckRequestsREST#getLoggedInUserRequests() to get all plagiarism check requests
         * made by or to the logged in user.
         */
        service.getLoggedInUserRequests = function() {
            return $http.get(API_URL + 'getLoggedInUserRequests').then(function(response) {
                return response.data;
            }, function(response) {
                if(response.status > 0) {
                    return response.status + ':' + response.statusText;
                }
            });
        };

        /**
         * Call PlagCheckRequestsREST#getPendingNumberOfRequests() to get number of pending requests from other users
         * to the logged in user
         */
        service.getNumberOfPendingRequests = function() {
          return $http.get(API_URL + 'getPendingNumberOfRequests').then(function(response) {
              return response.data;
          });
        };

        /**
         * Call PlagCheckRequestsREST#rejectRequest() to update the DB item with the rejection flag
         * @param plagRequest the request object to reject
         */
        service.rejectRequest = function(plagRequest) {
            return Upload.upload({
                url : API_URL + 'rejectRequest',
                data : {
                    plagRequest: angular.toJson(plagRequest)
                }
            }).then(function(response) {
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
