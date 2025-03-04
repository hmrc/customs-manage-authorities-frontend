document.addEventListener('DOMContentLoaded', function() {
    const backLinks = document.querySelectorAll('.govuk-back-link');
    backLinks.forEach(function(backLink) {
        backLink.addEventListener('click', function(event) {
            event.preventDefault();
            window.history.back();
        });
    });
});
