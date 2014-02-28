(function ($) {
	$(document).ready(
		function () {
			initializeInputHandlers();
			setTab('mainTab', 'account');
			displayEmailWarning();
		}
	);
	
	function displayEmailWarning() {
		if ($('#userEmailInput').val()) {
			$('#emailWarningLabel').hide();
		} else {
			$('#emailWarningLabel').show();
		}
	}

	function initializeInputHandlers() {
		$('#mainTab ul li a').click(function(event) {
			setTab('mainTab', event.target.getAttribute('href').substring(1));
		});
		
		$('#saveAccountButton').on('click', null, {}, updateAccount);
		// $('#saveProfileButton').on('click', null, {}, updateProfile);
		$('#userEmailInput').change(displayEmailWarning);
		$('#logoutButton').on('click', null, {}, logout);
		
		$('#changePasswordButton').click(function() {
			$('#oldPwdInput').val('');
			$('#newPwdInput').val('');
			$('#newPwdRepeatInput').val('');
			$('#changePasswordPopup').bPopup({opacity: 0.7});
			$('#oldPwdInput').focus();
		});
		$('#changePasswordCancelButton').click(function() {
			$('#changePasswordPopup').bPopup().close();
		});
		$('#changePasswordSubmitButton').click(function() {
			var oldPassword = $('#oldPwdInput').val();
			var newPassword = $('#newPwdInput').val();
			var repeatedPassword =$('#newPwdRepeatInput').val();
			if (oldPassword && oldPassword.length > 0 && newPassword && newPassword.length > 0 && newPassword == repeatedPassword) {
				changePassword();
			} else {
				alert('Failed to update the password. Validate your input.');
			}
			$('#changePasswordPopup').bPopup().close();
		});
	}
	
	function setTab(id, tabRef) {
		var tabRefId = tabRef.charAt(0).toUpperCase() + tabRef.slice(1);
		$('#' + id + ' ul li a[href="#' + tabRef + '"]').addClass('current');
		$('#' + id + ' ul li a[href!="#' + tabRef + '"]').removeClass('current');
		$('#' + id + ' > div').hide();
		$('#' + id + tabRefId).show();
	}
	
	function changePassword() {
		document.forms.changePasswordForm.submit();
	}
	
	function updateAccount(event) {
		document.forms.accountForm.submit();
	}
	
	function logout(event) {
		window.location.href = 'manage?action=logout';
	}
}(jQuery));