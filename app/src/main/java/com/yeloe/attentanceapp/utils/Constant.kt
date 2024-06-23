package com.yeloe.attentanceapp.utils

object Constant {

    const val STUDENT_LOGIN = "Student"
    const val TEACHER_LOGIN = "Teacher"
    const val TEACHER_LOGIN_REVISED = "Teacher Revised"
    const val STUDENT_LOGIN_REVISED = "Student Revised"
    const val NOBODY = "nobody"

    // shared preferences
    const val TEACHER_USER_SHARED = "teacher_user_access"
    const val STUDENT_USER_SHARED = "student_user_access"
    const val USER_ACCESS_SHARED = "user_access"
    const val USER_SIGN_IN_PREFERENCES = "UserPreferences"
    const val USER_NOT_SIGN_IN = "UserNull"

    // delays
    const val SEARCH_MOVIE_TIME_DELAY = 500L
    const val SHARED_PREFERENCES_DELAY = 2000

    // tabs values
    const val CLASSES_TAB_VALUE = 2

    // fields name
    const val EMAIL = "email"
    const val UID = "uid"
    const val CLASSROOM_UID = "classroomUid"
    const val ALLOW_ATTENDANCE = "allowAttendance"
    const val TIMESTAMP = "timeStamp"
    const val COLLEGE = "classroomCollege"
    const val BRANCH = "classroomBranch"
    const val YEAR = "classroomYear"
    const val SEMESTER = "classroomSemester"
    const val TEACHER_UID = "teacherUid"
    const val CLASS_UIS = "classUid"


    // collections
    const val studentUserCollection = "student users"
    const val teacherUserCollection = "teacher users"
    const val teacherClassRoomCollection = "teacher classroom"
    const val studentClassRoomCollection = "student classroom"
    const val classesCollection = "class"
    const val joinClassroomCollection = "join classroom"
    const val attendance = "attendance"
    const val faces = "faces"


    // success
    const val SIGN_IN_SUCCESSFUL = "Sign in successfully"
    const val LOG_IN_SUCCESSFUL = "Log in successfully"
    const val FORGOT_PASSWORD_SUCCESSFUL = "Forgot password link is send to your registration gmail"


    // errors
    const val EMAIL_EMPTY = "Email cannot be empty"
    const val PASSWORD_EMPTY = "Password cannot be empty"
    const val CHECK_INTERNET_CONNECTION = "Please check you internet connection"
    const val UNABLE_TO_SIGN_IN = "Unable to sign in please try again"
    const val UNABLE_TO_LOGIN_IN = "Unable to sign in please try again"
    const val UNABLE_TO_FORGOT_PASSWORD_IN =
        "Unable to send forgot password link to your email please try again"

    // logs
    const val CREATE_ACCOUNT_LOG = "create account error/details"
    const val FACE_DETECTION_LOG = "Face Detection error/details note"
    const val REPOSITORY_LOG = "Repository error/details"
    const val TEACHER_LOG = "teacher log error/details"
    const val STUDENT_LOG = "student log error/details"

    // intents
    const val GETTING_IMAGE_INTENT = "image/*"

    // circle radius
    const val CIRCLE_RADIUS = 60.0

    // account error
    const val ACCOUNT_ALREADY_EXIST_ERROR = "The email address is already in use by another account."

    // teacher app bar
    const val TEACHER_CLASSROOM_HOME = "Teacher Classroom"
    const val TEACHER_CLASSROOM_CREATE = "Create Classroom"
    const val TEACHER_CLASSROOM_UPDATE = "Update Classroom"
    const val TEACHER_CLASS = "Teacher Class"
    const val TEACHER_CREATE_CLASS = "Create Class"
    const val TEACHER_ATTENDANCE_MARKED = "Mark Attendance"
    const val TEACHER_ATTENDANCE_ABSENT = "Mark Absent"
    const val TEACHER_PROFILE = "Profile"
    const val TEACHER_EDIT_PROFILE = "Edit Profile"
    const val ADD = "Add"
    const val UPDATE = "update"
    const val CREATE = "Create"
    const val KEYWORDS = "keywords"
    const val PRESENT = "Present"
    const val ABSENT = "Absent"
    const val NAME = "name"


    // student app bar
    const val STUDENT_CLASSROOM_HOME = "Student Classroom"
    const val JOIN_CLASSROOM = "Join Classroom"
    const val STUDENT_CLASS = "Student Class"
    const val JOIN = "Join"
    const val SHOW_ATTENDANCE = "Show Attendance"
    const val SHOW_ABSENT = "Show Absent"
    const val STUDENT_PROFILE = "Profile"
    const val STUDENT_EDIT_PROFILE = "Edit Profile"

    // colors
    const val transparent_light_blue_color = "#40235FA6"
    const val transparent_dark_blue_color = "#50003060"
    const val transparent_red_color = "#BA1A1A"


    //models
    const val TF_OD_API_INPUT_SIZE2: Int = 160
    const val MODEL_NAME = "facenet.tflite"

    val authErrors = mapOf(
        "admin-restricted-operation" to "This operation is restricted to administrators only.",
        "argument-error" to "",
        "app-not-authorized" to "This app, identified by the domain where it's hosted, is not authorized to use Firebase Authentication with the provided API key. Review your key configuration in the Google API console.",
        "app-not-installed" to "The requested mobile application corresponding to the identifier (Android package name or iOS bundle ID) provided is not installed on this device.",
        "captcha-check-failed" to "The reCAPTCHA response token provided is either invalid, expired, already used or the domain associated with it does not match the list of whitelisted domains.",
        "code-expired" to "The SMS code has expired. Please re-send the verification code to try again.",
        "cordova-not-ready" to "Cordova framework is not ready.",
        "cors-unsupported" to "This browser is not supported.",
        "credential-already-in-use" to "This credential is already associated with a different user account.",
        "custom-token-mismatch" to "The custom token corresponds to a different audience.",
        "requires-recent-login" to "This operation is sensitive and requires recent authentication. Log in again before retrying this request.",
        "dynamic-link-not-activated" to "Please activate Dynamic Links in the Firebase Console and agree to the terms and conditions.",
        "email-change-needs-verification" to "Multi-factor users must always have a verified email.",
        "email-already-in-use" to "The email address is already in use by another account.",
        "expired-action-code" to "The action code has expired.",
        "cancelled-popup-request" to "This operation has been cancelled due to another conflicting popup being opened.",
        "internal-error" to "An internal error has occurred.",
        "invalid-app-credential" to "The phone verification request contains an invalid application verifier. The reCAPTCHA token response is either invalid or expired.",
        "invalid-app-id" to "The mobile app identifier is not registed for the current project.",
        "invalid-user-token" to "This user's credential isn't valid for this project. This can happen if the user's token has been tampered with, or if the user isn't for the project associated with this API key.",
        "invalid-auth-event" to "An internal error has occurred.",
        "invalid-verification-code" to "The SMS verification code used to create the phone auth credential is invalid. Please resend the verification code sms and be sure use the verification code provided by the user.",
        "invalid-continue-uri" to "The continue URL provided in the request is invalid.",
        "invalid-cordova-configuration" to "The following Cordova plugins must be installed to enable OAuth sign-in: cordova-plugin-buildinfo, cordova-universal-links-plugin, cordova-plugin-browsertab, cordova-plugin-inappbrowser and cordova-plugin-customurlscheme.",
        "invalid-custom-token" to "The custom token format is incorrect. Please check the documentation.",
        "invalid-dynamic-link-domain" to "The provided dynamic link domain is not configured or authorized for the current project.",
        "invalid-email" to "The email address is badly formatted.",
        "invalid-api-key" to "Your API key is invalid, please check you have copied it correctly.",
        "invalid-cert-hash" to "The SHA-1 certificate hash provided is invalid.",
        "invalid-credential" to "The supplied auth credential is malformed or has expired.",
        "invalid-message-payload" to "The email template corresponding to this action contains invalid characters in its message. Please fix by going to the Auth email templates section in the Firebase Console.",
        "invalid-multi-factor-session" to "The request does not contain a valid proof of first factor successful sign-in.",
        "invalid-oauth-provider" to "EmailAuthProvider is not supported for this operation. This operation only supports OAuth providers.",
        "invalid-oauth-client-id" to "The OAuth client ID provided is either invalid or does not match the specified API key.",
        "unauthorized-domain" to "This domain is not authorized for OAuth operations for your Firebase project. Edit the list of authorized domains from the Firebase console.",
        "invalid-action-code" to "The action code is invalid. This can happen if the code is malformed, expired, or has already been used.",
        "wrong-password" to "The password is invalid or the user does not have a password.",
        "invalid-persistence-type" to "The specified persistence type is invalid. It can only be local, session or none.",
        "invalid-phone-number" to "The format of the phone number provided is incorrect. Please enter the phone number in a format that can be parsed into E.164 format. E.164 phone numbers are written in the format [+][country code][subscriber number including area code].",
        "invalid-provider-id" to "The specified provider ID is invalid.",
        "invalid-recipient-email" to "The email corresponding to this action failed to send as the provided recipient email address is invalid.",
        "invalid-sender" to "The email template corresponding to this action contains an invalid sender email or name. Please fix by going to the Auth email templates section in the Firebase Console.",
        "invalid-verification-id" to "The verification ID used to create the phone auth credential is invalid.",
        "invalid-tenant-id" to "The Auth instance's tenant ID is invalid.",
        "multi-factor-info-not-found" to "The user does not have a second factor matching the identifier provided.",
        "multi-factor-auth-required" to "Proof of ownership of a second factor is required to complete sign-in.",
        "missing-android-pkg-name" to "An Android Package Name must be provided if the Android App is required to be installed.",
        "auth-domain-config-required" to "Be sure to include authDomain when calling firebase.initializeApp(), by following the instructions in the Firebase console.",
        "missing-app-credential" to "The phone verification request is missing an application verifier assertion. A reCAPTCHA response token needs to be provided.",
        "missing-verification-code" to "The phone auth credential was created with an empty SMS verification code.",
        "missing-continue-uri" to "A continue URL must be provided in the request.",
        "missing-iframe-start" to "An internal error has occurred.",
        "missing-ios-bundle-id" to "An iOS Bundle ID must be provided if an App Store ID is provided.",
        "missing-multi-factor-info" to "No second factor identifier is provided.",
        "missing-multi-factor-session" to "The request is missing proof of first factor successful sign-in.",
        "missing-or-invalid-nonce" to "The request does not contain a valid nonce. This can occur if the SHA-256 hash of the provided raw nonce does not match the hashed nonce in the ID token payload.",
        "missing-phone-number" to "To send verification codes, provide a phone number for the recipient.",
        "missing-verification-id" to "The phone auth credential was created with an empty verification ID.",
        "app-deleted" to "This instance of FirebaseApp has been deleted.",
        "account-exists-with-different-credential" to "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.",
        "network-request-failed" to "A network error (such as timeout, interrupted connection or unreachable host) has occurred.",
        "no-auth-event" to "An internal error has occurred.",
        "no-such-provider" to "User was not linked to an account with the given provider.",
        "null-user" to "A null user object was provided as the argument for an operation which requires a non-null user object.",
        "operation-not-allowed" to "The given sign-in provider is disabled for this Firebase project. Enable it in the Firebase console, under the sign-in method tab of the Auth section.",
        "operation-not-supported-in-this-environment" to "This operation is not supported in the environment this application is running on. \"location.protocol\" must be http, https or chrome-extension and web storage must be enabled.",
        "popup-blocked" to "Unable to establish a connection with the popup. It may have been blocked by the browser.",
        "popup-closed-by-user" to "The popup has been closed by the user before finalizing the operation.",
        "provider-already-linked" to "User can only be linked to one identity for the given provider.",
        "quota-exceeded" to "The project's quota for this operation has been exceeded.",
        "redirect-cancelled-by-user" to "The redirect operation has been cancelled by the user before finalizing.",
        "redirect-operation-pending" to "A redirect sign-in operation is already pending.",
        "rejected-credential" to "The request contains malformed or mismatching credentials.",
        "second-factor-already-in-use" to "The second factor is already enrolled on this account.",
        "maximum-second-factor-count-exceeded" to "The maximum allowed number of second factors on a user has been exceeded.",
        "tenant-id-mismatch" to "The provided tenant ID does not match the Auth instance's tenant ID",
        "timeout" to "The operation has timed out.",
        "user-token-expired" to "The user's credential is no longer valid. The user must sign in again.",
        "too-many-requests" to "We have blocked all requests from this device due to unusual activity. Try again later.",
        "unauthorized-continue-uri" to "The domain of the continue URL is not whitelisted.  Please whitelist the domain in the Firebase console.",
        "unsupported-first-factor" to "Enrolling a second factor or signing in with a multi-factor account requires sign-in with a supported first factor.",
        "unsupported-persistence-type" to "The current environment does not support the specified persistence type.",
        "unsupported-tenant-operation" to "This operation is not supported in a multi-tenant context.",
        "unverified-email" to "The operation requires a verified email.",
        "user-cancelled" to "The user did not grant your application the permissions it requested.",
        "user-not-found" to "There is no user record corresponding to this identifier. The user may have been deleted.",
        "user-disabled" to "The user account has been disabled by an administrator.",
        "user-mismatch" to "The supplied credentials do not correspond to the previously signed in user.",
        "user-signed-out" to "",
        "weak-password" to "The password must be 6 characters long or more.",
        "web-storage-unsupported" to "This browser is not supported or 3rd party cookies and data may be disabled."
    )


}