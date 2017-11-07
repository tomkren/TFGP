function mkServerInfoView($container) {

    var KEY_version = 'version';

    function render(initState) {

        var version = initState !== null ? initState[KEY_version] : '<span class="errorBox">Server is probably turned off.</span>';

        var $versionDiv = $('<div>').append([': Fishtron control panel (v ', version,')']);
        $container.html('').append($versionDiv);
    }

    return {render:render};
}
