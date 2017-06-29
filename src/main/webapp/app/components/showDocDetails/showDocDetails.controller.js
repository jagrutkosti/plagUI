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
        vm.docDetails = docDetails;
        vm.url = '';
        vm.iconTitles = ['Submitted to Plagchain', 'Submitted to Originstamp', 'Submitted to Bitcoin', 'Included in a block on Bitcoin', 'Timestamped successfully on Blockchain!'];
        vm.downloadSeed = downloadSeed;

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
