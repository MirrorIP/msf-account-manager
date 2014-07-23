(function ($) {
	var referrer = null;
	
	$(document).ready(
		function () {
			referrer = getURLParameter('ref');
			initializeInputHandlers();
			switch (location.hash.split('?')[0]) {
			case '#login':
				setTab('welcomeTab', 'login');
				break;
			case '#register':
				setTab('welcomeTab', 'register');
				break;
			case '#services':
				setTab('welcomeTab', 'services');
				break;
			default:
				setTab('welcomeTab', 'login');
			}
		}
	);

	function initializeInputHandlers() {
		$('#loginButton').on('click', null, {}, loginUser);
		$('#registerButton').on('click', null, {}, registerUser);
		$('#resetAccountButton').on('click', null, {}, resetUserAccount);
		
		$('#welcomeTab ul li a').click(function(event) {
			setTab('welcomeTab', event.target.getAttribute('href').substring(1));
		});
		
		$('#requestResetPopupClose').click(function() {
			$('#requestResetPopup').bPopup().close();
		});
		
		$('#noEmailCancelButton').click(function() {
			$('#noEmailConfirmationPopup').bPopup().close();
		});
		
		$('#warnInvalidUserIdPopup button').click(function() {
			$('#warnInvalidUserIdPopup').bPopup().close();
		})
		
		$('#warnNoPasswordPopup button').click(function() {
			$('#warnNoPasswordPopup').bPopup().close();
		});
		
		$('#warnPasswordsNotEqualPopup button').click(function() {
			$('#warnPasswordsNotEqualPopup').bPopup().close();
		});
		
		$('#infoRegistrationSuccessfulPopup button').click(function() {
			window.location.href = referrer ? referrer : 'manage';
		});
		
		$('#warnMissingUserIdPopup button').click(function() {
			$('#warnMissingUserIdPopup').bPopup().close();
		});
		
		$('#newUserIdInput').keypress(function(e) {
			supressInput(/[0-9a-z-_.]/, e);
	    });
	}
	
	function supressInput(regex, e) {
		// Allow: backspace, delete, tab, escape, enter and .
		if ($.inArray(e.keyCode, [46, 8, 9, 27, 13, 190]) !== -1 ||
			// Allow: Ctrl+A
			(e.keyCode == 65 && e.ctrlKey === true) || 
			 // Allow: home, end, left, right
			(e.keyCode >= 35 && e.keyCode <= 39)) {
			// let it happen, don't do anything
			return;
		}
		// Ensure that it is a number and stop the keypress
		if (!regex.test(String.fromCharCode(e.which))) {
	    	e.preventDefault();
	    }
	}
	
	function getURLParameter(name) {
		return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g, '%20')) || null;
	}
	
	function setTab(id, tabRef) {
		var tabRefId = tabRef.charAt(0).toUpperCase() + tabRef.slice(1);
		$('#' + id + ' ul li a[href="#' + tabRef + '"]').addClass('current');
		$('#' + id + ' ul li a[href!="#' + tabRef + '"]').removeClass('current');
		$('#' + id + ' > div').hide();
		$('#' + id + tabRefId).show();
	}
	
	function loginUser(event) {
		var locale = getURLParameter('locale');
		if (locale) {
			// $('input[name="locale"]').val(locale);
			$('form[name="loginForm"]').attr('action', 'manage?locale=' + locale);
		}
		document.forms.loginForm.submit();
	}
	
	function registerUser(event) {
		var userId = $('#newUserIdInput').val();
		var userPwd = $('#newUserPwdInput').val();
		var userPwdRepeat = $('#newUserPwdRepeatInput').val();
		var userEmail = $('#newEmailInput').val();
		var userName = $('#newFullNameInput').val();
		var isUserNamePublic = $('#newFullNamePublic').is(':checked');
		if (!userId || userId.length < 5) {
			$('#warnInvalidUserIdPopup').bPopup({opacity: 0.7});
			return;
		}
		if (!userPwd || userPwd.length == 0) {
			$('#warnNoPasswordPopup').bPopup({opacity: 0.7});
			return;
		} else if (userPwd != userPwdRepeat) {
			$('#warnPasswordsNotEqualPopup').bPopup({opacity: 0.7});
			return;
		}
		if (!userEmail || userEmail.length == 0) {
			$('#noEmailConfirmationPopup').bPopup({opacity: 0.7});
			$('#noEmailConfirmationButton').unbind();
			$('#noEmailConfirmationButton').click(function() {
				$('#noEmailConfirmationPopup').bPopup().close();
				performRegistration(userId, userPwd, userName, userEmail, isUserNamePublic);
			});
		} else {
			performRegistration(userId, userPwd, userName, userEmail, isUserNamePublic);
		}
	}
	
	function performRegistration(userId, userPwd, userName, userEmail, isUserNamePublic) {
		var data = {};
		data.action = 'register-account';
		data.userId = userId;
		data.userPwd = userPwd;
		if (userName) data.userName = userName;
		if (userEmail) data.userEmail = userEmail;
		if (isUserNamePublic) data.isUserNamePublic = 'true';
		$.post('manage', data, function(response) {
			var resultElement = response.firstChild;
			if (!resultElement) {
				showMessagePopup('Failed to request server.', 'warn');
			} else switch (resultElement.getAttribute('type')) {
			case 'result':
				$('#infoRegistrationSuccessfulPopup').bPopup({opacity: 0.7});
				break;
			case 'error':
				showMessagePopup(resultElement.textContent, 'warn');
				break;
			default:
				showMessagePopup('Unknown server response.', 'warn');
			}
		}, 'xml');
	}
	
	function showMessagePopup(message, type, actions) {
		var messageDiv = $('#messagePopupMessage');
		messageDiv.removeClass();
		switch (type) {
		case 'info':
			messageDiv.addClass('infoLabel');
			break;
		case 'warn':
			messageDiv.addClass('warnLabel');
			break;
		}
		messageDiv.html(message);
		
		$('#messagePopupButtons').empty();
		if (actions) {
			$.each(actions, function(index, action) {
				var actionButton = $('<button></button>');
				actionButton.html(action.label);
				actionButton.click(action.onclick);
				$('#messagePopupButtons').append(actionButton);
			});
		} else {
			var closeButton = $('<button>Close</button>');
			closeButton.attr('type','button');
			$('#messagePopupButtons').append(closeButton);
			closeButton.click(function() {
				$('#messagePopup').bPopup().close();
			});
		}
		$('#messagePopup').bPopup({opacity: 0.7});
	}
	
	function resetUserAccount(event) {
		var userId = $('#resetUserIdInput').val();
		if (userId && userId.length > 0) {
			var params = {action: 'reset-password', userId: userId};
			var locale = getURLParameter('locale'); 
			if (locale) {
				params.locale = locale;
			}
			$.post('manage', params, function(data, status) {
				if (status == 'success') {
					$('#requestResetResponse').html(data);
				} else {
					$('#requestResetResponse').html('<div class="warnLabel">REQUEST ERROR</div>');
				}
				$('#requestResetPopup').bPopup({opacity: 0.7});
			}, 'html');
		} else {
			$('#warnMissingUserIdPopup').bPopup({opacity: 0.7});
		}
	}
}(jQuery));