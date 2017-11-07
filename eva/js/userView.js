function mkUserView($container, dispatch) {


    function render(userData) {

        $container.html('').append([
            mkUserBox(userData['username'])
        ]);

    }

    function mkUserBox(username) {
        var $but = $('<button>').text('change user').addClass('hidden').click(function(){
            $but.addClass('hidden');
            var newUser = $input.val();
            if (_.size(newUser) > 0) {
                changeUser(newUser);
            }
        });

        function showBut() {
            $but.removeClass('hidden');
        }

        var $input = $('<input>').val(username).addClass('userInput').change(showBut).on("keydown", showBut);

        return $('<div>').append(['user ', $input, $but]);
    }

    function changeUser(newUser) {
        dispatch({type: 'CHANGE_USER', username: newUser});
    }

    return {render:render};

}