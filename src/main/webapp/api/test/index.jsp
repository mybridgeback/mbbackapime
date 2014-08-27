<!DOCTYPE HTML>
<%@page import="org.json.JSONArray, org.json.JSONObject, co.mybridge.MBPeople, co.mybridge.DBUtils" %>
<%
    MBPeople mp = new MBPeople();
    JSONArray ja = DBUtils.retrieveObjects(null, "mb_person", mp, "no");
%>
<html>
<head>
   <style>
    /* all */
    ::-webkit-input-placeholder { color:#f00; }
    ::-moz-placeholder { color:#f00; } /* firefox 19+ */
    :-ms-input-placeholder { color:#f00; } /* ie */
    input:-moz-placeholder { color:#f00; }
  </style>
  <script language="JavaScript">
  function autoPSSubmit() {
      document.getElementById("submit").disabled = true;
      var pid = document.getElementById("personId").value;
      if (pid.length > 5) {
          document.personform.action="/api/people/" + pid;
      }
      // enable the button right before the submit, otherwise, the submit does not work.
      document.getElementById("submit").disabled = false;
      document.getElementById("personform").submit();
  }
  function autoCollSubmit() {
      document.getElementById("collsubmit").disabled = true;
      var pid = document.getElementById("collPersonId").value;
      if (pid.length > 5) {
          document.collectionform.action="/api/people/" + pid + "/collections";
      }
      // enable the button right before the submit, otherwise, the submit does not work.
      document.getElementById("collsubmit").disabled = false;
      document.getElementById("collectionform").submit();
  }
  function autoKnowlSubmit() {
      document.getElementById("knowlsubmit").disabled = true;
      var pid = document.getElementById("knowlPersonId").value;
      var cid = document.getElementById("collectionId").value;
      if (pid.length > 5 && cid.length > 5) {
          document.knowledgeform.action="/api/people/" + pid + "/collections/" + cid + "/knowledge/";
      }
      alert("my path " + "/api/people/" + pid + "/collections/" + cid + "/knowledge/");
      // enable the button right before the submit, otherwise, the submit does not work.
      document.getElementById("knowlsubmit").disabled = false;
      document.getElementById("knowledgeform").submit();
  }
  </script>
</head>
<body>
<h1>Testing data entry</h1>
<div id="sec1">
<h2>Add or modify a user</h2>
<form name="personform" id="personform" action="/api/people" method="post">
<table width="80%" border="0">
<tr><td colspan="4"><h2><center>
<select name="personId" id="personId">
<option value="">Create a new user</option>
<%
    for (int i = 0; i< ja.length(); i++) {
        JSONObject jobj = ja.getJSONObject(i);
%>
        <option value="<%= jobj.getString("_id") %>">change  <%= jobj.getString("fullName") %></option>
<%
    }      
%>

</select>
</center></h2></td></tr>

<tr><th> Email</th><th>Password</th><th>Name</th><th>Industry</th><th>Profession</th></tr>
<tr><td align="center">
<input type="text" name="email" size="20" placeholder="email"> </input>
</td><td align="center">
<input type="password" name="password" size="14" placeholder="password"> </input>
</td>
<td align="center">
<input type="text" name="fullName" size="14" placeholder="Your Name"> </input>
</td><td align="center">
<select id="industry" name="industry">
    <option value="Internet/Computer Software">Internet/Computer Software</option>
</select>
</td><td align="left">
<input type="checkbox" name="profession" value="Startup">Start Up</input><br>
<input type="checkbox" name="profession" value="CEO">CEO</input><br>
<input type="checkbox" name="profession" value="Sales">Sales</input><br>
<input type="checkbox" name="profession" value="Marketing">Marketing</input><br>
<input type="checkbox" name="profession" value="Developer">Developer</input>
</td></tr>
<tr><td align="left" colspan="4">&nbsp;&nbsp;&nbsp;<b>Profile image URL:</b> 
<input type="text" name="thumbImage" size="45" placeholder="Enter Image URL here"></input>
</td></tr>
<tr><td colspan="2" >&nbsp;&nbsp;&nbsp;<b>Position:</b> <input type="text" size="20" name="position" placeholder="optional"></input></td>
<td colspan="3" ><b>Company:</b> <input type="text" size="20" name="company" placeholder="optional"></input></td></tr>
<tr><td colspan="5" align="center"><input type="submit" value="Submit" id="submit" name="submit" 
    onClick="javascript:autoPSSubmit();"></td></tr>
</table>
</form>
</div>
<br>
<div id="sec2">
<h2>Add a collection</h2>

<form name="collectionform" id="collectionform" action="/api/people/" method="post">
<table width="80%" border="0">
<tr><td ><h2><center>
<select name="collPersonId" id="collPersonId">
<%
    for (int i = 0; i< ja.length(); i++) {
        JSONObject jobj = ja.getJSONObject(i);
%>
        <option value="<%= jobj.getString("_id") %>">coll for  <%= jobj.getString("fullName") %></option>
<%
    }      
%>

</select>
</center></h2></td></tr>

<tr><td>Collection Title: <input type="text" name="collectionTitle" value=""></td></tr>

<tr><td align="center"><input type="submit" value="Submit" id="collsubmit" name="collsubmit" 
      onClick="javascript:autoCollSubmit();"></td></tr>
</table>
</form>

</div>

<div id="sec3">
<h2>Add a knowledge</h2>
<form name="knowledgeform" id="knowledgeform" action="/api/people/" method="post">
<table width="80%" border="0">
<tr><td ><h2><center>
<select name="knowlPersonId" id="knowlPersonId">
<%
    for (int i = 0; i< ja.length(); i++) {
        JSONObject jobj = ja.getJSONObject(i);
%>
        <option value="<%= jobj.getString("_id") %>">knowledge for  <%= jobj.getString("fullName") %></option>
<%
    }      
%>

</select>
</center></h2></td></tr>

<tr><td>Collection ID: <input type="text" name="collectionId" size="80" id="collectionId" value=""></td></tr>
<tr><td>Knowledge Title: <input type="text" name="title" size="80" value=""></td></tr>
<tr><td>My Own description: <input type="text" name="customDescription" size="100" value=""></td></tr>
<tr><td>From Source: <input type="text" name="contentSource" placeholder="such as \"from Youtube\"" value=""></td></tr>
<tr><td>External URL: <input type="text" name="externalURL"  value=""></td></tr>
<tr><td>HTML Body: <input type="text" name="htmlBody"  size="120" value=""></td></tr>
<tr><td>Image URL: <input type="text" name="thumbImage"  size="80" value=""></td></tr>
<tr><td>Content Width: <input type="text" name="width"  size="8" value=""></td></tr>
<tr><td>Content Height: <input type="text" name="height"  size="8" value=""></td></tr>
<tr><td align="center"><input type="submit" value="Submit" id="knowlsubmit" name="knowlsubmit" 
onClick="javascript:autoKnowlSubmit();"></td></tr>
</table>
</form>

</div>
<br>
</body></html>
