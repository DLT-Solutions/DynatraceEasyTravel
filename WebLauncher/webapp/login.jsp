<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<jsp:useBean id="userBean" class="com.dynatrace.easytravel.weblauncher.beans.LoggedInUserBean" scope="request"/>

<!DOCTYPE html>
<html>
<head>
    <title></title>
</head>
<body>
<center>
	<h4>easyTravel Configuration Web-UI</h4>
		<form method="POST" action="login" />
		   <table>
		        <tr>
		            <td>Username</td>
		            <td><input type="text" name="user" style='width:12em'></td>
		        </tr>
		        <tr>
		            <td>Password</td>
		            <td><input type="password" name="pass" style='width:12em'></td>
		        </tr>
		        <tr>
		            <td></td><td align="center">
		        <input type="submit" value="Login"></td></tr></table>
	   </form>
        </br>

        <c:if test="${userBean.loggedIn == 'true'}" >
            <b>User <%= userBean.getUserName() %> is currently logged in. </b>
            <b>Last activity: <%= userBean.getLastAccessTime() %> </b>
        </c:if>

	 </center>
</body>
</html>

