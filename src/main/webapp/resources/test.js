//'use strict';

function uniqueId() {
	var date = Date.now();
	var random = Math.random() * Math.random();

	return (Math.floor(date * random)%1000).toString();
};


function placeMessage(msg) {

	var message_html = document.createElement('li');
	message_html.setAttribute('id', "message-" + msg.id);
	var ownerClass = state.user == msg.user ? "my" : "other";
	var ownerInfoClass = state.user == msg.user ? "myInfo" : "otherInfo";
	var htmlAsText = '<li class="freespace" ></li>'
		+ '<div class="' + ownerInfoClass + '">' + msg.user + '</div>'
		+ '<div class="text-wrapper ' + ownerClass + '">'
		+ '<span id="message-' + msg.id + '-text" class="message-text">' + msg.text + '</span></br>'
		+ '<button class="glyphicon glyphicon-edit message-edit" data-msg-id="' + msg.id + '" aria-hidden="true"></button>'
		+ '<button class="glyphicon glyphicon-trash message-remove" aria-hidden="true" data-msg-id="' + msg.id+ '"></button>'
		+ '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'
		+ '</div>';

	message_html.innerHTML = htmlAsText;
	document.getElementById("conversation").appendChild(message_html);
}

window.state = {
	messages: [],
	user: "username-" + uniqueId(),
	mainUrl : 'http://192.168.0.101:999/chat',
	token : 'TE11EN'
};

function toQuery(data) {
	var tail = [];
	for (var key in data) {
		tail.push(key + '=' + data[key])
	}
	return tail.join('&');
}

function appendUrl(url, data) {
	return url + '?' + toQuery(data);
}

function ajax(method, url, data, success, error) {
	var xhr = new XMLHttpRequest();

	if (method.toLowerCase() == 'get') {
		url = appendUrl(url, data);
	}

	xhr.open(method, url, true);

	xhr.onreadystatechange = function () {
		if (xhr.readyState == 4) {
			if (200 <= xhr.status && xhr.status < 300) {
				var data;
				if (xhr.responseText.length > 0) {
					data = JSON.parse(xhr.responseText);
				}
				success(data)
			} else {
				//error
			}
		}
	};

	xhr.send(JSON.stringify(data));
}

function addMessage(message) {
	state.messages.push(message);
	placeMessage(message);
	checkRemoved(message);
}

function findStored(new_id) {
	for (var i in state.messages) {
		if (state.messages[i].id === new_id) {
			return i;
		}
	}
}

function markAsRemoved(message) {
	var el = document.getElementById("message-" + message.id);
	var classes = el.className.split(' ');
	if (classes.indexOf('message-removed') == -1) {
		classes.push('message-removed');
		el.className = classes.join(' ');
	}
}

function updateText(message) {
	var el = document.getElementById("message-" + message.id);
	var classes = el.className.split(' ');
	if (classes.indexOf('message-edited') === -1) {
		classes.push('message-edited');
		el.className = classes.join(' ');
	}
	el.getElementsByClassName("message-text")[0].innerHTML = message.text;
}

function checkRemoved(message) {
	if (message.removed) {
		markAsRemoved(message);
	}
}
function updateMessage(i, message) {
	state.messages[i] = message;

	checkRemoved(message);
	updateText(message);
}

function isMessageChanged(old_mes, new_mes) {
	return (old_mes.body !== new_mes) || (old_mes.removed && !new_mes.removed);
}

function handleMessage(message) {
	var i = findStored(message.id);

	if (i === undefined) {
		addMessage(message);
	} else if (isMessageChanged(state.messages[i], message)) {
		updateMessage(i, message);
	}
}

function updateWithScroll(update) {
	var conversation = document.getElementById("conversation"),
		shouldScroll = Math.abs(conversation.scrollTop + conversation.offsetHeight - conversation.scrollHeight) < 10;

	update();

	if (shouldScroll) {
		conversation.scrollTop = conversation.scrollHeight;
	}
}

function loadMessages() {
	ajax("GET", state.mainUrl, {token: state.token}, function(data) {
		updateWithScroll(function() {
			var messages = data.messages;
			for (var index in messages) {
				handleMessage( messages[index] );
			}
		});
	});
}

function messageTextInput() {
	return document.getElementById("new-message");
}

function createMessage(text, success) {
	var message = {user: state.user, text: text, token: state.token};

	ajax("POST", state.mainUrl, message, success);
}

function putMessage(messageId, text, success) {
	var input = {id: messageId, text: text};
	ajax("PUT", state.mainUrl, input, success);
}

function deleteMessage(message_id) {
    ajax("DELETE", state.mainUrl, {id: message_id}, loadMessages);
}

function cleanMessageInputCallback(messageInput) {
	return function() {
		loadMessages();
		messageInput.value = "";
		messageInput.dataset.messageId = "";
		messageInput.focus();
	}
}

function createOrUpdateMessage() {
	var messageInput = messageTextInput(),
		messageId = messageInput.dataset.messageId,
	    success = cleanMessageInputCallback(messageInput),
		text = messageInput.value;

	if (text === "") {
		return;
	}

    if (messageId === undefined || messageId === "") {
        createMessage(text, success);
	} else {
		putMessage(messageId, text, success);
	}
}

function editOrDeleteMessage(e) {
	e.preventDefault();
	var classes = e.target.className.split(' ');
    if (classes.indexOf("message-edit") > -1) {
		var messageInput = messageTextInput(),
			message_id = e.target.dataset.msgId,
			message = state.messages[ findStored(message_id) ];
		messageInput.value = message.text;
		messageInput.dataset.messageId = message_id;
	} else if (classes.indexOf("message-remove") > -1) {
		deleteMessage(e.target.dataset.msgId);
	}
}

function refreshUsername() {
	state.user = document.getElementById("username").value;
}

function setFirstUsernameInInput() {
	var input = document.getElementById("username");
	input.value = state.user;
}

function run() {
	setFirstUsernameInInput();
	loadMessages();
	setInterval(loadMessages, 1000);

	var newMessageButton = document.getElementById("new-message-button");
	newMessageButton.addEventListener("click", createOrUpdateMessage);

	var convWindow = document.getElementById("conversation");
	convWindow.addEventListener("click", editOrDeleteMessage);

	var refreshUsernameBtn = document.getElementById("new-username-btn");
	refreshUsernameBtn.addEventListener("click", refreshUsername)
}