document.addEventListener("DOMContentLoaded", function () {
    const togglePassword = document.querySelector('.input-group-text a');
    const passwordInput = document.querySelector('input[name="password"]');

    togglePassword.addEventListener('click', function (event) {
        event.preventDefault();
        if (passwordInput.type === "password") {
            passwordInput.type = "text";
            this.title = "Hide password";
        } else {
            passwordInput.type = "password";
            this.title = "Show password";
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const usernameInput = document.querySelector('input[name="username"]');
    const passwordInput = document.querySelector('input[name="password"]');
    const loginButton = document.querySelector('button[name="login"]');

    // Disabilita il bottone all'inizio
    loginButton.disabled = true;

    function toggleButtonState() {
        if (usernameInput.value.trim() !== "" && passwordInput.value.trim() !== "") {
            loginButton.disabled = false;
        } else {
            loginButton.disabled = true;
        }
    }

    usernameInput.addEventListener("input", toggleButtonState);
    passwordInput.addEventListener("input", toggleButtonState);
});