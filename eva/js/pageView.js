function mkPageView($container, dispatch) {

    var KEY_name = 'name';
    var KEY_selected = 'selected';
    var KEY_containerId = 'containerId';

    function render(pages, currentPageIndex) {
        $container.html('').append(mkTabs(pages));
        for (var i = 0; i < pages.length; i++) {
            var containerId = pages[i][KEY_containerId];
            if (i === currentPageIndex) {
                $('#'+containerId).removeClass('hidden');
            } else {
                $('#'+containerId).addClass('hidden');
            }
        }




    }


    function mkTabs(pages) {
        return $('<div>').append(_.map(pages, mkPageTab));
    }

    function mkPageTab(pageData) {
        var tabName = pageData[KEY_name];
        if (pageData[KEY_selected]) {tabName = _.toUpper(tabName);}

        function selectTab() {
            dispatch({type:'SELECT_PAGE', name: pageData[KEY_name]});
        }

        return $('<button>').addClass('tab').text(tabName).click(selectTab);
    }


    return {render: render};
}
