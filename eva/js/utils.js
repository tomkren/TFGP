function log(x) {
    console.log(x);
}

function onEnter($el, callback) {
    return $el.keypress(function(e) {
        if (e.which === 13) {
            callback(e);
        }
    });
}
