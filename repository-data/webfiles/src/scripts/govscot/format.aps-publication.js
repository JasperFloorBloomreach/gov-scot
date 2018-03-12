'use strict';

define([
    './pubsub',
    '../shared/component.page-group',
    './component.display-toggle',
    './component.sticky-back-to-top',
    '../shared/component.sticky-document-info'
], function(pubsub, pageGroup) {

    var publicationPage = {},
        pages = {},
        isMobile = $('.toc-mobile-trigger').is(':visible'),
        pgroup;

    publicationPage.init = function() {
        this.initExpandables();
        this.initAsyncNavigation();
        this.initStickyInfoInteractivity();
        this.initSidebarHeight();
        pgroup = pageGroup.init();
    };

    publicationPage.initSidebarHeight = function () {
        publicationPage.setSidebarHeight();

        $(window).on('resize', function() {
            publicationPage.setSidebarHeight();
        });
    };

    publicationPage.setSidebarHeight = function () {
        var publicationBody = $('.publication-info__body'),
            targetHeight = 0;

        if (window.innerWidth >= 768) {
            targetHeight = Math.min($('.js-content-wrapper').height(), window.innerHeight);
        }

        publicationBody.css({minHeight: targetHeight});
    };

    publicationPage.initExpandables = function () {
        $('.js-expand-downloads').on('click', function (event) {
            event.preventDefault();

            var target = $($(this).attr('href')),
                linkText = $(this).find('.publication-info__preamble-expand');

            target.slideToggle(200);

            linkText.text(linkText.text() === 'More' ? 'Less' : 'More');
        });
    };

    publicationPage.initStickyInfoInteractivity = function() {

        function scrollListTop() {
            var offset = $('.page-group').offset().top + 3; /** MAGIC NUM!! */
            window.scrollTo(null, offset);
        }

        /**
         * Open or close the pageGroup list with buttons in sticy doc info
         */
        $('.js-mobile-toc-trigger-open').click(function(){
            pgroup.open();
            scrollListTop();
        });
        $('.js-mobile-toc-trigger-close').click(function(){
            pgroup.close();
            scrollListTop();
        });

        /* Hide close button to start with */
        $('.js-mobile-toc-trigger-close').hide();

        /**
         * Update which button to show based on page group state
         */
        pubsub.subscribe('page-group-change', function(event, state){
            if (state === 'open') {
                $('.js-mobile-toc-trigger-close').css('display', 'inline-block');
                $('.js-mobile-toc-trigger-open').hide();
            } else {
                $('.js-mobile-toc-trigger-open').css('display', 'inline-block');
                $('.js-mobile-toc-trigger-close').hide();
            }
        });

         $('.sticky-document-info__trigger').click(function(){
            var button = $(this),
                expandClass = 'sticky-document-info__trigger--expanded',
                panel = button.next(),
                openClass = 'sticky-document-info__panel--open',
                isOpen = panel.hasClass(openClass),
                isSticky = $('.sticky-document-info').hasClass('sticky-document-info--is-sticky'),
                buttonText;

            if (isSticky) {
                buttonText = 'All files';
                panel.find('.primary-doc').show()
                  .next().find('h3').show();
            } else {
                buttonText = 'Supporting files';
                panel.find('.primary-doc').hide()
                  .next().find('h3').hide();
            }
            button.text(buttonText);

            if ( !isOpen ) {
                // Add classes
                panel.addClass(openClass);
                button.addClass(expandClass);
            } else {
                // Remove classes
                panel.removeClass(openClass);
                button.removeClass(expandClass);
                // blur button
                button.blur();
            }
         });
    };

    /**
     * Init the async navigation in progressive enhancement approach
     */
    publicationPage.initAsyncNavigation = function () {
        /* We initially prevent default action, but if we encounter an error
         * loading the data/page then we fallback to redirecting to the clicked
         * link.
         */

         $('.js-content-wrapper').on('click', '.js-contents a, .js-previous, .js-next', function (e) {
            var linkEl = $(this),
                url = linkEl.attr('href');
            if (url) {
                event.preventDefault();

                publicationPage.loadSubPageHtml(url)
                    .done(function(){
                        linkEl.blur();
                        if (isMobile && linkEl.hasClass('page-group__link')) {
                            pgroup.close();
                        };
                    })
                    .fail(function(){
                        window.location = url;
                    });
            }
        });

        /**
         * Catch changes in state
         */
        window.onpopstate = function(e){
            if(e.state){
                var url = e.target.location.pathname;
                publicationPage.loadSubPage(url, false)
                    .fail(function(){
                        window.location = url;
                    });
            }
        };

        /**
         * Add a class to pinpoint the top edge of the subpage for scrolling
         * to the right location after page loaded.
         */
        if ( isMobile ) {
            $('.publication-content').addClass('js-subpage-top-edge');
        } else {
            $('.js-sticky-header-position ').addClass('js-subpage-top-edge');
        }
    };

    /**
     * Load the HTML for the page and caches it in a local variable.
     * Returns a promise that will return successful if the request was
     * successful or there already was a hit in the cache.
     */
    publicationPage.loadHtml = function (url) {

        var deferred = jQuery.Deferred();

        if (pages[url]) {
            deferred.resolve(pages[url]);
        } else {
            var request = new XMLHttpRequest();
            request.open('GET', url, true);

            request.onreadystatechange = function() {
                if (this.readyState === 4) {
                    if (this.status >= 200 && this.status < 400) {
                        pages[url] = this.responseText;
                        deferred.resolve(pages[url]);
                    } else {
                        deferred.reject();
                    }
                }
            };

            request.send();
            request = null;
        }

        return deferred.promise();
    };

    /**
     * Loads the given subpage
     * @param url
     * @param updateHistory
     */
    publicationPage.loadSubPageHtml = function (url, updateHistory) {
        var deferred = jQuery.Deferred();

        if (typeof(updateHistory) === 'undefined') {
            updateHistory = true;
        }

        this.loadHtml(url)
            .done(function (data) {
                var bodyContentWrapper = document.querySelector('.js-content-wrapper');

                // parse the HTML string and extract the part we want
                var element = document.createElement('div');
                element.insertAdjacentHTML('beforeend', data);
                var newContents = element.querySelector('.js-content-wrapper');

                // insert new HTML
                bodyContentWrapper.innerHTML = newContents.innerHTML;

                // scroll to top of content
                var targetOffset = $('.js-content-wrapper').offset().top - parseInt($('.sticky-document-info').height(), 10);
                if (window.scrollY > targetOffset) {
                    window.scrollTo(window.scrollX, targetOffset);
                }

                // Update the URL and history
                if (updateHistory && window.history.pushState) {
                    window.history.pushState({
                        'html': 'inner html?',
                        'pageTitle': 'updated page title'
                    },'', url);
                }
            })
            .fail(function () {
                deferred.reject();
            });

        return deferred.promise();
    };

    window.format = publicationPage;
    window.format.init();

    return publicationPage;
});