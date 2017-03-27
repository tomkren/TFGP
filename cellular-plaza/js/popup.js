function mkPopup($el) {

    $el.addClass('popup');

    var $msg = $('<div>');
    var $close = $('<button>')
        .text('X')
        .addClass('popup-close')
        .click(close);

    $el.append($msg,$close);

    function setPos(e) {
        var x = e.pageX + 20;
        var y = e.pageY - 5;

        $el.css({
            left: x+'px',
            top:  y+'px'
        });
    }

    function show(e, str) {
        setPos(e);
        $msg.text(str);
        $el.show();
    }

    function close() {
        $el.hide();
    }

    return {
        show : show
    };
}
