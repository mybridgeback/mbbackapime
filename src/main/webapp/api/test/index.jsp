<!DOCTYPE HTML>
<%@page import="org.json.JSONArray, org.json.JSONObject, org.apache.commons.lang.StringEscapeUtils,  
        co.mybridge.MBPeople, co.mybridge.MBCollections, co.mybridge.DBUtils" %>
<%
    MBPeople mp = new MBPeople();
    JSONArray javaPeople = DBUtils.retrieveObjects(null, "mb_person", mp, "no");
    MBCollections mc = new MBCollections();
    JSONArray javaCollections = DBUtils.retrieveObjects(null, "mb_collection", mc, "no");
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
  jsCollections=eval('<%=StringEscapeUtils.escapeJavaScript(javaCollections.toString())%>');
  jsPeople=eval('<%=StringEscapeUtils.escapeJavaScript(javaPeople.toString())%>');
  </script>
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
      // enable the button right before the submit, otherwise, the submit does not work.
      document.getElementById("knowlsubmit").disabled = false;
      document.getElementById("knowledgeform").submit();
  }
  
  function populateCollections(personDropDown) {
      var pid = personDropDown.value;
      var collOpt = "";
      for (var x=0; x<jsCollections.length; x++) {
           
          var coll = jsCollections[x];
              var collpid = coll.personId;
              if (collpid == pid) {
                  var collId = coll._id;
                  var collTitle = coll.collectionTitle;
                  collOpt = collOpt + '\n<option value=\"' + collId + '\">' + collTitle + '</option>';
              }
      }
      document.getElementById("collectionId").innerHTML = collOpt;
  }
  function populateExistingColls(personDropDown) {
      var pid = personDropDown.value;
      var collOpt = "";
      for (var x=0; x<jsCollections.length; x++) {
           
          var coll = jsCollections[x];
              var collpid = coll.personId;
              if (collpid == pid) {
                  var collTitle = coll.collectionTitle;
                  if (collOpt == "") {
                      collOpt = collTitle;
                  } else {
                      collOpt = collOpt + '&nbsp;;&nbsp;&nbsp;' + collTitle;
                  }
              }
      }
      document.getElementById("existColls").innerHTML = collOpt;
  }
  function populatePerson(personDropDown) {
      var pid = personDropDown.value;
      for (var x=0; x<jsPeople.length; x++) {    
          var coll = jsPeople[x];
          var jspid = coll._id;
          if (jspid == pid) {
              var email = coll.email;
              var fullName = coll.fullName;
              var position = coll.position;
              var company = coll.company;
              var thumbImage = coll.thumbImage;
              var education = coll.education;
              var professions = coll.professions;
              
              if (email != null && email.length > 1) {
              	  personform.email.value = email;
              } else {
                  personform.email.value = '';
              }
              personform.fullName.value = fullName;
              if (position != null && position.length > 0) {
                  personform.position.value = position;
              } else {
                  personform.position.value = '';
              }
              if (company != null && company.length > 0) {
                  personform.company.value = company;
              } else {
                  personform.company.value = '';
              }
              personform.thumbImage.value = thumbImage;
              if (education != null && education.length > 1) {
                  personform.education.value = education;
              } else {
                  personform.education.value = '';
              }
                 
              var profform = personform.profession;
              for (j=0; j< profform.length; j++) {
                  profform[j].checked = false;
              }
              if (professions.length > 0) {
                  for (i=0; i<professions.length; i++) {
                 		var prof = professions[i];
                   		for (j=0; j< profform.length; j++) {
                   		    if (profform[j].value == prof) {
                   		        profform[j].checked = true;
                   		    }
                   		}
                  }
              }
              break;
          }
      }
  }
  </script>
</head>
<body>
<h1>Testing data entry</h1>
<div id="sec1">
<h2>Add or modify a user</h2>
<form name="personform" id="personform" action="/api/people" method="post">
<table width="90%" border="0" align="center">
<tr><td colspan="4"><h2>
<select name="personId" id="personId" onchange="populatePerson(this);">
<option value="">Create a new user</option>
<%
    for (int i = 0; i< javaPeople.length(); i++) {
        JSONObject jobj = javaPeople.getJSONObject(i);
%>
        <option value="<%= jobj.getString("_id") %>">Update   <%= jobj.getString("fullName") %></option>
<%
    }      
%>

</select>
</h2></td></tr>

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
<td colspan="2" ><b>Company:</b> <input type="text" size="20" name="company" placeholder="optional"></input></td>
<td><b>Education: </b><input type="text" size="10" name="education" placeholder="optional"></input></td></tr>
<tr><td colspan="5" align="center"><input type="submit" value="Submit" id="submit" name="submit" 
    onClick="javascript:autoPSSubmit();"></td></tr>
</table>
</form>
</div>
<br>
<hr>
<div id="sec2">
<h2>Add a collection</h2>

<form name="collectionform" id="collectionform" action="/api/people/" method="post">
<table width="80%" border="0" align="center">
<tr><td >
<select name="collPersonId" id="collPersonId" onchange="populateExistingColls(this);">
<option value="" disabled selected style="display:none;">Select a person</option>
<%
    for (int i = 0; i< javaPeople.length(); i++) {
        JSONObject jobj = javaPeople.getJSONObject(i);
%>
        <option value="<%= jobj.getString("_id") %>">  <%= jobj.getString("fullName") %></option>
<%
    }      
%>

</select>
</td></tr>
<tr><td><b>Existing collections: </b><span id="existColls">  </span><td><tr>
<tr><td>New collection title: <input type="text" name="collectionTitle" size="40" value=""></td></tr>

<tr><td align="center"><input type="submit" value="Submit" id="collsubmit" name="collsubmit" 
      onClick="javascript:autoCollSubmit();"></td></tr>
</table>
</form>

</div>
<hr>
<div id="sec3">
<h2>Add a knowledge</h2>
<form name="knowledgeform" id="knowledgeform" action="/api/people/" method="post">
<table width="80%" border="0" align="center">
<tr><td >
<select name="knowlPersonId" id="knowlPersonId" onchange="populateCollections(this);">
<option value="" disabled selected style="display:none;">Select a person</option>
<%
    for (int i = 0; i< javaPeople.length(); i++) {
        JSONObject jobj = javaPeople.getJSONObject(i);
%>
        <option value="<%= jobj.getString("_id") %>">knowledge for  <%= jobj.getString("fullName") %></option>
<%
    }      
%>

</select></td>
<td >
<select name="collectionId" id="collectionId">

</select></td>
</tr>

<tr><td>Knowledge Title: </td><td><input type="text" name="title" size=60" value=""></td></tr>
<tr><td>My Own description: </td><td> <input type="text" name="customDescription" size="60" value=""></td></tr>
<tr><td>From Source: </td><td> <input type="text" name="contentSource" size="20"  value="YouTube"></td></tr>
<tr><td>Publisher: </td><td> <input type="text" name="publisher" size="40"  value=""></td></tr>
<tr><td>Type: </td><td> <input type="text" name="contentType" size="20"  value="Video"></td></tr>
<tr><td>External URL:  </td><td><input type="text" name="externalURL" size="70" value=""></td></tr>
<tr><td>HTML Body:  </td><td><input type="text" name="htmlBody"  size="70" value=""></td></tr>
<tr><td>Thumb URL:  </td><td><input type="text" name="thumbImage"  size="60" value=""></td></tr>
<tr><td>Content Width:  </td><td><input type="text" name="width"  size="8" value=""></td></tr>
<tr><td>Content Height:  </td><td><input type="text" name="height"  size="8" value=""></td></tr>
<tr><td align="center" colspan=2><input type="submit" value="Submit" id="knowlsubmit" name="knowlsubmit" 
onClick="javascript:autoKnowlSubmit();"></td></tr>
</table>
</form>

</div>
<br>
</body></html>
