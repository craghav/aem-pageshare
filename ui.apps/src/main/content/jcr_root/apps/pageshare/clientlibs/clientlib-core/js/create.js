(function (document, $) {
    "use strict";

    var expirationDate = "#expirationDate > input";
    var submit = "#sharepageform-submit";
    var userPickerEl = "#sharepage-userpicker";
    var userPickerErrorToolTip, userPickerErrorIcon;



    $(document).on("click", submit, function (e) {
        e.preventDefault();

        var $form = $("#sharepageform");
        var contentPath = $form.prop("action");
        var formData = $form.serialize();

        $.ajax({
            type: "POST",
            url: Granite.HTTP.externalize(contentPath + ".html"),
            contentType: $form.prop("enctype"),
            data: formData,
            cache: false
        }).done(function (data, textStatus, jqXHR) {
            var params = new URLSearchParams(window.location.search);
            if (params.has('redirect')) {
                window.location = params.get('redirect');
            }
        }).fail(function (jqXHR, textStatus, errorThrown) {
            //show the error
            var errorModal = new Coral.Dialog().set({
                id: "sharepage-error-dialog",
                variant: "error",
                closable: "on",
                header: {
                    innerHTML: Granite.I18n.get("Page Share Failed")
                },
                content: {
                    innerHTML: Granite.I18n.get(jqXHR.responseText)
                },
                footer: {
                    innerHTML: '<button is="coral-button" class="closeExport" variant="default" coral-close>' + Granite.I18n.get("Close") + '</button>'
                }
            })
            $("body").append(errorModal);
            errorModal.show();
        });
    });

    $(document).on("change", "#emailsubject, #expirationDate ", function (e) {
        checkSubmit();
    });

    $(document).on("click", ".linkshare-user", function (e) {
        $("#add-new-user").trigger("click");
    });

    $(document).on("click", "#add-new-user", function (e) {
        var $this = $(this);
        var uriTemplate = $this.data("foundationCollectionAction").data.href;
        var userPicker = $("#sharepage-userpicker");
        var inputText = $("input[is='coral-textfield']", userPicker);
        var inputAssignee = $("input[type='hidden']", userPicker);
        var userId = inputAssignee.val().trim();
        var result = Granite.$.ajax({
                        type: "GET",
                        async: false,
                        url: Granite.URITemplate.expand(uriTemplate, {query: userId, searchById: "true"})
                    });

        if (userId.length > 0 && result.status == 200) {
                var userDetails = $($(result.responseText)[0]);
                var id = userDetails.data("value");
                var email = userDetails.data("email");
                if (id && isEmailValid(email)) {
                    var members = $("#sharepage-members");
                    if ($(members).find('th').length < 1) {
                        var header = "<tr><th></th><th>" + Granite.I18n.get("TITLE") + "</th><th>" + Granite.I18n.get("EMAIL") + "</th> <th></th></tr>"
                        if ($(members).find('tbody').length > 0) {
                            $(members).find('tbody').prepend(header);
                        }
                    }
                    removeDuplicateandAddNewMember(members, id, email, userDetails);
                }
                //remove the text from the text box and disable the add button
                inputText.val('');
                checkSubmit();

        } else if (userId.length > 0 ) {
             var externalUserMail = userId;
             var avatar = Granite.HTTP.externalize("/libs/granite/security/clientlib/themes/default/resources/sample-user-thumbnail.36.png");
             var userData = $("<li class=\"coral-SelectList-item coral-SelectList-item--option foundation-layout-flexmedia\" data-value=\"external-anon-user\" data-display=\"" + inputText + "\" data-name=\"External User\" data-email=\"" + externalUserMail + "\"><img class=\"foundation-layout-flexmedia-img\" width=\"32\" height=\"32\" src=\"" + avatar +"\"><div class=\"foundation-layout-flexmedia-bd\"><div class=\"foundation-layout-flexmedia-bd-singleline\">External User</div><div class=\"foundation-layout-flexmedia-bd-singleline foundation-layout-util-subtletext\">external-anon-user</div></div></li>")
             var uId = userData.data("value");
             if (uId && isEmailValid(externalUserMail)) {
                 var spMembers = $("#sharepage-members");
                 if ($(spMembers).find('th').length < 1) {
                     var bodyContent = "<tr><th></th><th>" + Granite.I18n.get("TITLE") + "</th><th>" + Granite.I18n.get("EMAIL") + "</th> <th></th></tr>"
                     if ($(spMembers).find('tbody').length > 0) {
                         $(spMembers).find('tbody').prepend(bodyContent);
                     }
                 }
                 removeDuplicateandAddNewMember(members, id, externalUserMail, userData);
             }
             inputText.val('');
             checkSubmit();

         }
    });

    function removeDuplicateandAddNewMember(members, id, email, userDetails) {
        var duplicateFound = false;
        var members = $("#sharepage-members");
        if (id == "external-anon-user") {
            if (userDetails.data("email").length > 0) {
                id = userDetails.data("email");
            }
        }
        members.find("tr").each(function () {
            var m = $(this);
            var mID = m.find(".email input[name=\"email\"]").val();
            if (email != undefined && mID != undefined && email.toLowerCase() == mID.toLowerCase()) {
                duplicateFound = true;
                return;
            }
        });

        if (!duplicateFound) {
            addMember(members, id, userDetails);
        }
    }

    function addMember(members, id, userDetails) {
        var avatar = $("img", userDetails).attr("src");
        if (!avatar) {
            avatar = Granite.HTTP.externalize("/libs/granite/security/clientlib/themes/default/resources/sample-user-thumbnail.36.png");
        }
        var name = userDetails.data("name");
        var email = userDetails.data("email");
        var userid = userDetails.data("value");
        //create the markup with these values
        var member = $("<tr>");
        var cavatar = $("<td class=\"avatar\"><img src=\"" + avatar + "\" width=\"42\"></td>");
        var displayLocName = name;
        if (name == "External User") {
            displayLocName = Granite.I18n.get("External User");
        }
        var cName = $("<td class=\"name\">" + displayLocName + "</td>");
        var cEmail = $("<td class=\"email\"> <input type=\"hidden\" name=\"principalName\" value=\"" + userid + "\"> <input type=\"hidden\" name=\"email\" value=\"" + email + "\"> <span class=\"greyText\"></span>" + email + "</td>");
        var cRemove = $("<td class=\"remove\"><button title=\"" + Granite.I18n.get("Remove") + "\"class=\"coral-Button coral-Button--quiet\" type=\"button\"><i class=\"coral-Icon coral-Icon--sizeXS coral-Icon--closeCircle \"></i></button></td>");
        member.append(cavatar);
        member.append(cName);
        if (email && email.length > 0) {
            member.append(cEmail);
        }
        member.append(cRemove);
        members.append(member);
        var numusers = $("#sharepage-members").find(".avatar").length;
        if (numusers == 1) {
            hideUserPickerRequiredError();
        }
    }

    $(document).on("click", "#sharepage-members .remove", function () {
        var member = $(this).closest("tr");
        if (member) {
            member.remove();
            var numusers = $("#sharepage-members").find(".avatar").length;
            if (numusers <= 0) {
                showUserPickerRequiredError();
                var members = $("#sharepage-members");
                if ($(members).find('tbody').length > 0) {
                    $(members).find('tbody').html('');
                }
                checkSubmit();
            }
        }
    });


    $("#sharepage-userpicker input[type='text']").on("change", function (e) {
        handleAdd();
    });

    $(document).on("blur", "#sharepage-userpicker input[type='text']", function (e) {
        var numusers = $("#sharepage-members").find(".avatar").length;
        if (numusers <= 0) {
            showUserPickerRequiredError();
        }
    });

    $(document).on("selected", ".linkshare-user", function (e) {
        handleAdd();
    });

    function handleAdd() {
        var text = $("#sharepage-userpicker input[type='text']").val();
        var addUser = $("#add-new-user");
        if (text.length > 0) {
            addUser.removeAttr('disabled');
        } else {
            addUser.attr('disabled', 'disabled')
        }
    }

    function isEmailValid(email) {
        var re = /^([\w-\+]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
        return re.test(email);
    }

    function checkSubmit() {
        var $emailsubject = $(emailsubject);
        var $expirationDate = $(expirationDate);
        var $emailMessage = $(sharepagemessage);
        var $pagepath = $(pagepath);
        var $submit = $(submit);
        var subject = $emailsubject.val().trim();
        var exDate = $expirationDate.val().trim();
        var msg = $emailMessage.val().trim();
        var pgpath = $pagepath.val().trim();
        var numusers = $("#sharepage-members").find(".avatar").length;
        if (subject === "" || exDate === "" || msg === "" || pgpath === "" || numusers <= 0) {
            $submit.attr('disabled', 'disabled');
        } else {
            $submit.removeAttr('disabled');
        }
    }

    function showUserPickerRequiredError() {
        var $userPickerEl = $(userPickerEl);
        $userPickerEl.find("input[type='text']").addClass("is-invalid");
        $userPickerEl.css("padding-top", "1.6875rem");
        if (!userPickerErrorIcon) {
            userPickerErrorIcon = new Coral.Icon().set({
                id: "user-picker-fielderror-icon",
                icon: "alert",
                size: "S"
            });
            userPickerErrorIcon.className += ' coral-Form-fielderror error-info-icon';
        }
        $userPickerEl.prepend(userPickerErrorIcon);
        if (!userPickerErrorToolTip) {
            userPickerErrorToolTip = new Coral.Tooltip().set({
                variant: 'error',
                content: {
                    innerHTML: Granite.I18n.get("Please fill out this field.")
                },
                target: '#user-picker-fielderror-icon',
                placement: 'left',
                id: "user-picker-fielderror-tooltip"
            });
        }
        $userPickerEl.prepend(userPickerErrorToolTip);
    }

    function hideUserPickerRequiredError() {
        var $userPickerEl = $(userPickerEl);
        $userPickerEl.find("input[type='text']").removeClass("is-invalid");
        $userPickerEl.css("padding-top", "0");
        $userPickerEl.find("#user-picker-fielderror-icon").remove();
        $userPickerEl.find("#user-picker-fielderror-tooltip").remove();
    }



})(document, Granite.$);