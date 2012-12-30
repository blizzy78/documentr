<%--
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012-2013 Maik Schreiber

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>

<!DOCTYPE html>
<html>

<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>

<title><spring:message code="title.formattingHelp"/> &ndash; documentr</title>

<meta name="viewport" content="width=device-width, initial-scale=1.0">

<jsp:include page="/WEB-INF/view/requireConfig.jsp"/>

<link rel="stylesheet" href="<c:url value="/css/documentr.css"/>" media="all"/>

<script type="text/javascript" src="<c:url value="/js/require-2.1.2.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/jquery-1.8.3.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-2.2.2.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/documentr.js"/>"></script>

<body id="#top" class="markdown-help">

<c:set var="lang" value="${d:getLanguage()}"/>

<c:choose>
	<c:when test="${lang eq 'de'}">
		<h2>Formatierungshilfe</h2>
		
		<p>Klicken Sie auf einen der Bereiche, um Hilfe zur Formatierung zu erhalten.</p>
	</c:when>
	<c:otherwise>
		<h2>Formatting Help</h2>
		
		<p>Click on any of the sections to get help on formatting.</p>
	</c:otherwise>
</c:choose>

<div class="accordion" id="accordion">
	<div class="accordion-group">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#formatting">
				<c:choose>
					<c:when test="${lang eq 'de'}">Allgemeine Formatierung</c:when>
					<c:otherwise>General Formatting</c:otherwise>
				</c:choose>
			</a>
		</div>
		<div id="formatting" class="accordion-body collapse in">
			<div class="accordion-inner">
				<c:choose>
					<c:when test="${lang eq 'de'}">
						<table class="table table-documentr table-bordered table-condensed">
							<tbody>
								<tr>
									<td width="50%"><strong>Fett</strong></td>
									<td width="50%"><code>**Fett**</code></td>
								</tr>
								<tr>
									<td><em>Kursiv</em></td>
									<td><code>*Kursiv*</code></td>
								</tr>
								<tr>
									<td><h2>&Uuml;berschrift 1</h2></td>
									<td><pre><code>&Uuml;berschrift 1<br/>=============</code></pre><em class="alternative">oder</em><code># &Uuml;berschrift 1</code></td>
								</tr>
								<tr>
									<td><h3>&Uuml;berschrift 2</h3></td>
									<td><pre><code>&Uuml;berschrift 2<br/>-------------</code></pre><em class="alternative">oder</em><code>## &Uuml;berschrift 2</code></td>
								</tr>
								<tr>
									<td><h4>&Uuml;berschrift 3</h4></td>
									<td><pre><code>### &Uuml;berschrift 3</code></pre></td>
								</tr>
								<tr>
									<td><h5>&Uuml;berschrift 4</h5></td>
									<td><pre><code>#### &Uuml;berschrift 4</code></pre></td>
								</tr>
								<tr>
									<td><h6>&Uuml;berschrift 5</h6></td>
									<td><pre><code>##### &Uuml;berschrift 5</code></pre></td>
								</tr>
							</tbody>
						</table>
					</c:when>
					<c:otherwise>
						<table class="table table-documentr table-bordered table-condensed">
							<tbody>
								<tr>
									<td width="50%"><strong>Bold</strong></td>
									<td width="50%"><code>**Bold**</code></td>
								</tr>
								<tr>
									<td><em>Italic</em></td>
									<td><code>*Italic*</code></td>
								</tr>
								<tr>
									<td><h2>Heading 1</h2></td>
									<td><pre><code>Heading 1<br/>=============</code></pre><em class="alternative">or</em><code># Heading 1</code></td>
								</tr>
								<tr>
									<td><h3>Heading 2</h3></td>
									<td><pre><code>Heading 2<br/>-------------</code></pre><em class="alternative">or</em><code>## Heading 2</code></td>
								</tr>
								<tr>
									<td><h4>Heading 3</h4></td>
									<td><pre><code>### Heading 3</code></pre></td>
								</tr>
								<tr>
									<td><h5>Heading 4</h5></td>
									<td><pre><code>#### Heading 4</code></pre></td>
								</tr>
								<tr>
									<td><h6>Heading 5</h6></td>
									<td><pre><code>##### Heading 5</code></pre></td>
								</tr>
							</tbody>
						</table>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</div>

	<div class="accordion-group">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#links">Links</a>
		</div>
		<div id="links" class="accordion-body collapse">
			<div class="accordion-inner">
				<c:choose>
					<c:when test="${lang eq 'de'}">
						<p>
						Allein stehende URLs werden automatisch erkannt. Sie können auch mit einem Linktext versehen werden.
						</p>
					
						<table class="table table-documentr table-bordered table-condensed">
							<tbody>
								<tr>
									<td width="50%"><a href="http://www.example.com">http://www.example.com</a></td>
									<td width="50%"><code>http://www.example.com</code><em class="alternative">oder</em><code>[[http://www.example.com]]</code></td>
								</tr>
								<tr>
									<td><a href="http://www.example.com">Gehe zu www.example.com</a></td>
									<td><code>[[http://www.example.com Gehe zu www.example.com]]</code></td>
								</tr>
								<tr>
									<td><a href="http://www.example.com" rel="nofollow">Link mit &quot;nofollow&quot;</a></td>
									<td><code>[[http://www.example.com Link mit &quot;nofollow&quot; | nofollow]]</code></td>
								</tr>
								<tr>
									<td><a href="mailto:info@example.com">info@example.com</a></td>
									<td><code>&lt;info@example.com&gt;</code></td>
								</tr>
								<tr>
									<td>mailto:<a href="mailto:info@example.com">info@example.com</a></td>
									<td><code>mailto:info@example.com</code></td>
								</tr>
								<tr>
									<td><a href="mailto:info@example.com">Mail senden</a></td>
									<td><code>[[mailto:info@example.com Mail senden]]</code></td>
								</tr>
							</tbody>
						</table>
					</c:when>
					<c:otherwise>
						<p>
						Standalone URLs are recognized automatically. They can also have a link text.
						</p>
					
						<table class="table table-documentr table-bordered table-condensed">
							<tbody>
								<tr>
									<td width="50%"><a href="http://www.example.com">http://www.example.com</a></td>
									<td width="50%"><code>http://www.example.com</code><em class="alternative">or</em><code>[[http://www.example.com]]</code></td>
								</tr>
								<tr>
									<td><a href="http://www.example.com">Go to www.example.com</a></td>
									<td><code>[[http://www.example.com Go to www.example.com]]</code></td>
								</tr>
								<tr>
									<td><a href="http://www.example.com" rel="nofollow">Link using &quot;nofollow&quot;</a></td>
									<td><code>[[http://www.example.com Link using &quot;nofollow&quot; | nofollow]]</code></td>
								</tr>
								<tr>
									<td><a href="mailto:info@example.com">info@example.com</a></td>
									<td><code>&lt;info@example.com&gt;</code></td>
								</tr>
								<tr>
									<td>mailto:<a href="mailto:info@example.com">info@example.com</a></td>
									<td><code>mailto:info@example.com</code></td>
								</tr>
								<tr>
									<td><a href="mailto:info@example.com">Send Mail</a></td>
									<td><code>[[mailto:info@example.com Send Mail]]</code></td>
								</tr>
							</tbody>
						</table>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</div>

	<div class="accordion-group">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#images">
				<c:choose>
					<c:when test="${lang eq 'de'}">Bilder</c:when>
					<c:otherwise>Images</c:otherwise>
				</c:choose>
			</a>
		</div>
		<div id="images" class="accordion-body collapse">
			<div class="accordion-inner">
				<c:choose>
					<c:when test="${lang eq 'de'}">
						<p>
						An eine Seite angehängte Bilder können direkt auf der Seite angezeigt werden, zum Beispiel:
						</p>
						
						<pre><code>![Firmenlogo](logo.jpg)</code></pre>
						
						<p>
						Der Teil zwischen <code>[ ]</code> ist der alternative Text für das Bild. In <code>( )</code>
						steht der Dateiname des Anhangs.
						</p>
						
						<p>
						Mit der Option <code>thumb</code> werden Bilder als Vorschaubilder angezeigt. Ein Klick
						auf das jeweilige Bild zeigt es im Original an. Zum Beispiel:
						</p>

						<pre><code>![Bild 1](image1.jpg | thumb)<br/>![Bild 2](image2.jpg | thumb)<br/>![Bild 3](image3.jpg | thumb)</code></pre>
						
						<p>
						Mehrere solcher Vorschaubilder hintereinander werden automatisch zusammen in einer Art
						Galerie angezeigt.
						</p>
					</c:when>
					<c:otherwise>
						<p>
						Image attachments of a page can be displayed on a page directly, for example:
						</p>
						
						<pre><code>![Company Logo](logo.jpg)</code></pre>
						
						<p>
						The part between <code>[ ]</code> is the alternative text for the image. The attachment file name
						goes between <code>( )</code>.
						</p>

						<p>
						The option <code>thumb</code> allows to display images as preview images. Clicking
						on these shows the original images. For example:
						</p>

						<pre><code>![Image 1](image1.jpg | thumb)<br/>![Image 2](image2.jpg | thumb)<br/>![Image 3](image3.jpg | thumb)</code></pre>
						
						<p>
						Multiple preview images directly following one another are displayed automatically in
						some sort of gallery.
						</p>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</div>

	<div class="accordion-group">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#code">
				<c:choose>
					<c:when test="${lang eq 'de'}">Quellcode</c:when>
					<c:otherwise>Source Code</c:otherwise>
				</c:choose>
			</a>
		</div>
		<div id="code" class="accordion-body collapse">
			<div class="accordion-inner">
				<c:choose>
					<c:when test="${lang eq 'de'}">
						<table class="table table-documentr table-bordered table-condensed">
							<tbody>
								<tr>
									<td width="50%"><code>Quellcode im Fließtext</code></td>
									<td width="50%"><code>`Quellcode im Fließtext`</code></td>
								</tr>
								<tr>
									<td><div class="code-view-wrapper">
										<!--__NOTRIM__--><div class="code-view">Zeile 1
Zeile 2
Zeile 3</div><!--__/NOTRIM__-->
</div></td>
									<td><pre><code>    Zeile 1<br/>    Zeile 2<br/>    Zeile 3</code></pre><em class="alternative">oder</em><pre><code>~~~<br/>Zeile 1<br/>Zeile 2<br/>Zeile 3<br/>~~~</code></pre></td>
								</tr>
								<tr>
									<td><div class="code-view-wrapper">
										<!--__NOTRIM__--><div class="code-view" data-type="html">&lt;!-- Quellcode mit
Hervorhebung --&gt;
&lt;p class=&quot;header&quot;&gt;
Hallo
&lt;p&gt;</div><!--__/NOTRIM__-->
</div></td>
									<td><pre><code>~~~html<br/>&lt;!-- Quellcode mit<br/>Hervorhebung --&gt;<br/>&lt;p class=&quot;header&quot;&gt;<br/>Hallo<br/>&lt;p&gt;<br/>~~~</code></pre></td>
								</tr>
								<tr>
									<td><div class="code-view-title">Ein Titel</div><div class="code-view-wrapper">
										<!--__NOTRIM__--><div class="code-view" data-type="html">&lt;!-- Quellcode mit
Hervorhebung und Titel --&gt;
&lt;p class=&quot;header&quot;&gt;
Hallo
&lt;p&gt;</div><!--__/NOTRIM__-->
</div></td>
									<td><pre><code>~~~html:Ein Titel<br/>&lt;!-- Quellcode mit<br/>Hervorhebung und Titel --&gt;<br/>&lt;p class=&quot;header&quot;&gt;<br/>Hallo<br/>&lt;p&gt;<br/>~~~</code></pre></td>
								</tr>
							</tbody>
						</table>
					</c:when>
					<c:otherwise>
						<table class="table table-documentr table-bordered table-condensed">
							<tbody>
								<tr>
									<td width="50%"><code>Source code within running text</code></td>
									<td width="50%"><code>`Source code within running text`</code></td>
								</tr>
								<tr>
									<td><div class="code-view-wrapper">
										<!--__NOTRIM__--><div class="code-view">Line 1
Line 2
Line 3</div><!--__/NOTRIM__-->
</div></td>
									<td><pre><code>    Line 1<br/>    Line 2<br/>    Line 3</code></pre><em class="alternative">or</em><pre><code>~~~<br/>Line 1<br/>Line 2<br/>Line 3<br/>~~~</code></pre></td>
								</tr>
								<tr>
									<td><div class="code-view-wrapper">
										<!--__NOTRIM__--><div class="code-view" data-type="html">&lt;!-- Source code
with highlighting --&gt;
&lt;p class=&quot;header&quot;&gt;
Hello
&lt;p&gt;</div><!--__/NOTRIM__-->
</div></td>
									<td><pre><code>~~~html<br/>&lt;!-- Source code<br/>with highlighting --&gt;<br/>&lt;p class=&quot;header&quot;&gt;<br/>Hallo<br/>&lt;p&gt;<br/>~~~</code></pre></td>
								</tr>
								<tr>
									<td><div class="code-view-title">A Title</div><div class="code-view-wrapper">
										<!--__NOTRIM__--><div class="code-view" data-type="html">&lt;!-- Source code
with highlighting and title --&gt;
&lt;p class=&quot;header&quot;&gt;
Hello
&lt;p&gt;</div><!--__/NOTRIM__-->
</div></td>
									<td><pre><code>~~~html:A Title<br/>&lt;!-- Source code<br/>with highlighting and title --&gt;<br/>&lt;p class=&quot;header&quot;&gt;<br/>Hallo<br/>&lt;p&gt;<br/>~~~</code></pre></td>
								</tr>
							</tbody>
						</table>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</div>

	<div class="accordion-group">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#lists">
				<c:choose>
					<c:when test="${lang eq 'de'}">Listen</c:when>
					<c:otherwise>Lists</c:otherwise>
				</c:choose>
			</a>
		</div>
		<div id="lists" class="accordion-body collapse">
			<div class="accordion-inner">
				<c:choose>
					<c:when test="${lang eq 'de'}">
						<p>
						Listenpunkte müssen zum Einrücken auf die nächsttiefere Ebene mit 4 Leerzeichen
						eingerückt werden.
						</p>
					
						<table class="table table-bordered table-condensed">
							<tbody>
								<tr>
									<td width="50%"><ul><li>Liste 1</li><li>Liste 2<ul><li>Liste 2.1</ul></li><li>Liste 3</li></ul></td>
									<td width="50%"><pre><code>- Liste 1<br/>- Liste 2<br/>    - Liste 2.1<br/>- Liste 3</code></pre><em class="alternative">oder</em><pre><code>* Liste 1<br/>* Liste 2<br/>    * Liste 2.1<br/>* Liste 3</code></pre></td>
								</tr>
								<tr>
									<td><ol><li>Liste 1</li><li>Liste 2<ol><li>Liste 2.1</ol></li><li>Liste 3</li></ol></td>
									<td><pre><code>1. Liste 1<br/>2. Liste 2<br/>    1. Liste 2.1<br/>3. Liste 3</code></pre></td>
								</tr>
								<tr>
									<td><dl><dt>Begriff 1</dt><dd>Beschreibung 1</dd><dt>Begriff 2</dt><dd>Beschreibung 2</dd></dl></td>
									<td><pre><code>Begriff 1<br/>: Beschreibung 1<br/><br/>Begriff 2<br/>: Beschreibung 2</code></pre></td>
								</tr>
							</tbody>
						</table>
					</c:when>
					<c:otherwise>
						<p>
						Bullet points must be indented by 4 spaces to go on the next level.
						</p>
					
						<table class="table table-bordered table-condensed">
							<tbody>
								<tr>
									<td width="50%"><ul><li>List 1</li><li>List 2<ul><li>List 2.1</ul></li><li>List 3</li></ul></td>
									<td width="50%"><pre><code>- List 1<br/>- List 2<br/>    - List 2.1<br/>- List 3</code></pre><em class="alternative">or</em><pre><code>* List 1<br/>* List 2<br/>    * List 2.1<br/>* List 3</code></pre></td>
								</tr>
								<tr>
									<td><ol><li>List 1</li><li>List 2<ol><li>List 2.1</ol></li><li>List 3</li></ol></td>
									<td><pre><code>1. List 1<br/>2. List 2<br/>    1. List 2.1<br/>3. List 3</code></pre></td>
								</tr>
								<tr>
									<td><dl><dt>Term 1</dt><dd>Definition 1</dd><dt>Term 2</dt><dd>Definition 2</dd></dl></td>
									<td><pre><code>Term 1<br/>: Definition 1<br/><br/>Term 2<br/>: Definition 2</code></pre></td>
								</tr>
							</tbody>
						</table>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</div>

	<div class="accordion-group">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#tables">
				<c:choose>
					<c:when test="${lang eq 'de'}">Tabellen</c:when>
					<c:otherwise>Tables</c:otherwise>
				</c:choose>
			</a>
		</div>
		<div id="tables" class="accordion-body collapse">
			<div class="accordion-inner">
				<table class="table table-bordered table-condensed">
					<tbody>
						<tr>
							<td width="50%"><table class="table table-bordered table-condensed table-striped"><thead><tr><th>A</th><th>B</th><th>C</th></tr></thead><tbody><tr><td>1</td><td>2</td><td>3</td></tr><tr><td>4</td><td>5</td><td>6</td></tr><tr><td>7</td><td>8</td><td>9</td></tr></tbody></table></td>
							<td width="50%"><pre><code>A | B | C<br/>--|---|--<br/>1 | 2 | 3<br/>4 | 5 | 6<br/>7 | 8 | 9</code></pre></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>

	<div class="accordion-group">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#misc">
				<c:choose>
					<c:when test="${lang eq 'de'}">Verschiedenes</c:when>
					<c:otherwise>Miscellaneous</c:otherwise>
				</c:choose>
			</a>
		</div>
		<div id="misc" class="accordion-body collapse">
			<div class="accordion-inner">
				<table class="table table-bordered table-condensed">
					<tbody>
						<tr>
							<td width="50%"><blockquote>Believe you can and you're halfway there.</blockquote></td>
							<td width="50%"><pre><code>&gt; Believe you can and<br/>&gt; you're halfway there.</code></pre></td>
						</tr>
						<tr>
							<td><hr/></td>
							<td><code>---</code></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>

</body>

</html>
