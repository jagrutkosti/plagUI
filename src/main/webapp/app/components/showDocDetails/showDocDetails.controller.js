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

    DocDetailsController.$inject = ['DocDetailsService', 'AlertService', 'docDetails', '$window'];

    function DocDetailsController(DocDetailsService, AlertService, docDetails, $window){
        var vm = this;
        vm.downloadSeed = downloadSeed;
        vm.docDetails = docDetails;
        vm.url = '';
        vm.progressOptions = {
            scale: {
                enabled: true,
                type: 'dots',
                color: 'gray',
                width: 2,
                quantity: 5
            },
            size: 90,
            trackWidth: 9,
            barWidth: 9,
            barCap: 10,
            step: 1,
            min:0,
            max:5,
            displayInput: false,
            readOnly: true,
            trackColor: 'rgba(52,152,219,.1)',
            barColor: 'rgba(255,193,7,1)'
        };
        /**
         * To download the seed as a text file.
         * @param seed the seed to download. Plagchain seed or Originstamp seed.
         */
        function downloadSeed(seed) {
            var blob = new Blob([ seed ], { type : 'text/plain' });
            vm.url = (window.URL || window.webkitURL).createObjectURL( blob );
        }
    }
})();
