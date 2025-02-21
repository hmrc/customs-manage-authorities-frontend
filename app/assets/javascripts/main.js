document.addEventListener('DOMContentLoaded', function() {
    const backLink = document.getElementById('browser-back-link');
    if (backLink) {
        backLink.addEventListener('click', function(event) {
            event.preventDefault();
            window.history.back();
        });
    }
});
