angular.module('angular-highlight', []).directive('highlight', function() {
    var component = function(scope, element, attrs) {

        if (!attrs.highlightClass) {
            attrs.highlightClass = 'angular-highlight';
        }

        var replacer = function(match, item) {
            return '<span class="'+attrs.highlightClass+'">'+match+'</span>';
        };
        var tokenize = function(keywords) {
            keywords = keywords.replace(new RegExp(',$','g'), '').split(',');
            for (var i = 0; i < keywords.length; i++) {
                keywords[i] = keywords[i].replace(new RegExp('^ | $','g'), '');
            }
            return keywords;
        };

        scope.$watch('keywords', function() {
            //console.log("scope.keywords",scope.keywords);
            if (!scope.keywords || scope.keywords === '') {
                element.html(scope.highlight);
                return false;
            }
            var tokenized	= tokenize(scope.keywords);
            var regex 		= new RegExp('\\S*(' + tokenized.join('|') + ')\\S*', 'gmi');

            // Find the words
            var html = scope.highlight.replace(regex, replacer);

            element.html(html);
        });
    };
    return {
        link: 			component,
        replace:		false,
        scope:			{
            highlight:	'=',
            keywords:	'='
        }
    };
});
