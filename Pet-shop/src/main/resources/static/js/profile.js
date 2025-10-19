Clerk.load({ apiKey: "pk_test_cG9saXRlLWRyYWtlLTc5LmNsZXJrLmFjY291bnRzLmRldiQ" });

async function loadProfile() {
    const user = Clerk.user;
    if (!user) {
        alert("Ви не авторизовані");
        return;
    }

    document.getElementById('profileName').textContent = user.fullName || user.firstName;
    document.getElementById('profileEmail').textContent = user.emailAddresses[0].emailAddress;
    document.getElementById('profileRoles').textContent = user.publicMetadata?.roles || "USER";

    const profileModal = new bootstrap.Modal(document.getElementById('profileModal'));
    profileModal.show();
}
const inventoryMenu = document.getElementById('inventory-menu');
const dropdown = inventoryMenu.querySelector('.dropdown-menu');

inventoryMenu.addEventListener('mouseenter', () => {
    inventoryMenu.classList.add('show');
});

inventoryMenu.addEventListener('mouseleave', () => {
    inventoryMenu.classList.remove('show');
});


