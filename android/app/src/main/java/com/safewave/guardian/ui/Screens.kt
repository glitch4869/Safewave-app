package com.safewave.guardian.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safewave.guardian.Contact
import com.safewave.guardian.Live
import com.safewave.guardian.Me
import com.safewave.guardian.Profile
import com.safewave.guardian.Rec
import com.safewave.guardian.Status
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------------- 공통 ----------------
data class Look(val color: Color, val icon: ImageVector, val title: String, val sub: String)

fun look(l: Live, name: String): Look = when (l.status) {
    Status.NORMAL -> Look(C.ok, Icons.Default.CheckCircle, "안전해요", "$name 님이 집에서 잘 지내고 계세요")
    Status.EMPTY -> Look(C.empty, Icons.Default.Home, "집에 안 계세요", "감지 구역에 사람이 없어요")
    Status.GRACE ->
        if (l.acking) Look(C.warn, Icons.Default.Check, "확인을 보냈어요", "센서가 정상으로 바뀌기를 기다리고 있어요")
        else Look(C.warn, Icons.Default.Warning, "넘어진 것 같아요",
            "반응을 확인하고 있어요" + (l.graceLeft?.let { " · ${it}초 남음" } ?: ""))
    Status.EMERGENCY ->
        if (l.acking) Look(C.warn, Icons.Default.Check, "확인을 보냈어요", "센서가 정상으로 바뀌기를 기다리고 있어요")
        else Look(C.crit, Icons.Default.Warning, "긴급! 넘어지셨어요", "$name 님이 움직이지 않으세요. 지금 확인해 주세요")
    Status.OFFLINE -> Look(C.off, Icons.Default.Info, "센서와 연결이 끊겼어요", "센서 전원과 와이파이를 확인해 주세요")
    Status.LOADING -> Look(C.off, Icons.Default.Refresh, "확인하는 중이에요", "센서 상태를 불러오고 있어요")
}

private val hm = SimpleDateFormat("a h:mm", Locale.KOREAN)
private val md = SimpleDateFormat("M월 d일", Locale.KOREAN)

fun timeText(ts: Long): String {
    if (ts <= 0) return ""
    val c = Calendar.getInstance(); val t = Calendar.getInstance().apply { timeInMillis = ts }
    val sameDay = c.get(Calendar.YEAR) == t.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == t.get(Calendar.DAY_OF_YEAR)
    c.add(Calendar.DAY_OF_YEAR, -1)
    val yesterday = c.get(Calendar.YEAR) == t.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == t.get(Calendar.DAY_OF_YEAR)
    val d = Date(ts)
    return when { sameDay -> "오늘 " + hm.format(d); yesterday -> "어제 " + hm.format(d); else -> md.format(d) + " " + hm.format(d) }
}

@Composable
fun Card(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.card)
            .border(BorderStroke(1.dp, C.line), RoundedCornerShape(20.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) { content() }
}

@Composable
fun Page(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(C.bg).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { content(); Spacer(Modifier.height(12.dp)) }
}

private fun kindColor(kind: String) = when (kind) { "ok" -> C.ok; "wn" -> C.warn; "cr" -> C.crit; else -> C.off }

@Composable
fun RecordRow(r: Rec) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 8.dp).size(14.dp).clip(CircleShape).background(kindColor(r.kind)))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(timeText(r.ts), fontSize = 16.sp, color = C.sub, fontWeight = FontWeight.SemiBold)
            Text(r.msg.removePrefix("[레이더] "), fontSize = 20.sp, color = C.ink, lineHeight = 28.sp)
        }
    }
}

// ---------------- 로그인 ----------------
@Composable
fun LoginScreen(busy: Boolean, error: String, onLogin: (String, String) -> Unit, onJoin: (String, String, String) -> Unit) {
    var join by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    Page {
        Spacer(Modifier.height(12.dp))
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(24.dp)).background(C.brand), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Text("SafeWave 보호자", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = C.ink)
        Body("관제 사이트와 같은 계정으로 들어가세요.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BigButton("로그인", { join = false }, Modifier.weight(1f), outlined = join, contentColor = if (join) C.brand else Color.White)
            BigButton("회원가입", { join = true }, Modifier.weight(1f), outlined = !join, contentColor = if (!join) C.brand else Color.White)
        }
        val fieldText = TextStyle(fontSize = 22.sp, color = C.ink)
        val colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = C.brand, unfocusedBorderColor = C.sub, focusedContainerColor = C.card, unfocusedContainerColor = C.card)
        if (join) OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("이름", fontSize = 18.sp) },
            textStyle = fieldText, singleLine = true, colors = colors)
        OutlinedTextField(email, { email = it.trim() }, Modifier.fillMaxWidth(), label = { Text("이메일", fontSize = 18.sp) },
            textStyle = fieldText, singleLine = true, colors = colors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        OutlinedTextField(pw, { pw = it }, Modifier.fillMaxWidth(), label = { Text("비밀번호", fontSize = 18.sp) },
            textStyle = fieldText, singleLine = true, colors = colors,
            visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = { TextButton({ show = !show }) { Text(if (show) "숨기기" else "보기", fontSize = 18.sp) } })
        if (error.isNotEmpty()) Body(error, color = C.crit)
        BigButton(
            if (busy) "잠시만요..." else if (join) "가입하기" else "로그인",
            { if (join) onJoin(name, email, pw) else onLogin(email, pw) },
            enabled = !busy && email.isNotBlank() && pw.isNotBlank() && (!join || name.isNotBlank()),
            icon = Icons.Default.Lock
        )
        if (join) Body("가입하면 관리자가 승인한 뒤부터 쓸 수 있어요.", size = 18)
    }
}

@Composable
fun WaitScreen(title: String, desc: String, onRetry: (() -> Unit)?, onLogout: () -> Unit) {
    Page {
        Spacer(Modifier.height(40.dp))
        Box(Modifier.size(96.dp).clip(CircleShape).background(C.warn), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(56.dp))
        }
        Title(title)
        Body(desc)
        if (onRetry != null) BigButton("다시 확인하기", onRetry, icon = Icons.Default.Refresh)
        BigButton("로그아웃", onLogout, outlined = true, contentColor = C.sub)
    }
}

// ---------------- 홈 ----------------
@Composable
fun StatusCard(l: Live, name: String, onAck: () -> Unit, ackBusy: Boolean) {
    val k = look(l, name)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(k.color).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(k.icon, null, tint = Color.White, modifier = Modifier.size(72.dp))
        Text(k.title, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 48.sp)
        Text(k.sub, fontSize = 23.sp, color = Color.White, lineHeight = 32.sp)
        if (l.test) Text("※ 사이트에서 시험 중입니다 (실제 낙상 아님)", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
        if (l.status == Status.OFFLINE && l.lastSeen > 0) Text("마지막 신호: ${timeText(l.lastSeen)}", fontSize = 19.sp, color = Color.White)
        if (l.connMsg.isNotEmpty() && (l.status == Status.OFFLINE || l.status == Status.LOADING)) Text(l.connMsg, fontSize = 18.sp, color = Color.White)
        if ((l.status == Status.EMERGENCY || l.status == Status.GRACE) && !l.acking) {
            Spacer(Modifier.height(4.dp))
            BigButton(if (ackBusy) "보내는 중..." else "확인했어요 (알람 끄기)", onAck, color = Color.White, contentColor = k.color,
                icon = Icons.Default.Check, enabled = !ackBusy)
        }
    }
}

@Composable
fun HomeScreen(
    l: Live, p: Profile, recs: List<Rec>, ackBusy: Boolean, ackMsg: String,
    onAck: () -> Unit, onCall: (Contact) -> Unit, on119: () -> Unit, onInfo: () -> Unit, onRecords: () -> Unit
) {
    Page {
        Text("${p.name} 님", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = C.ink)
        StatusCard(l, p.name, onAck, ackBusy)
        if (ackMsg.isNotEmpty()) Body(ackMsg, color = if (ackMsg.startsWith("확인")) C.ok else C.crit)
        val first = p.contacts.firstOrNull { it.phone.isNotBlank() }
        if (first != null) BigButton("${first.rel.ifEmpty { first.name }}에게 전화", { onCall(first) }, color = C.ok, icon = Icons.Default.Phone)
        BigButton("119 전화", on119, color = C.crit, icon = Icons.Default.Call)
        BigButton("어르신 정보 보기", onInfo, outlined = true, contentColor = C.ink, icon = Icons.Default.Person)
        Card {
            Text("최근 기록", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = C.ink)
            if (recs.isEmpty()) Body("아직 기록이 없어요.")
            recs.take(3).forEach { RecordRow(it) }
            if (recs.size > 3) TextButton(onRecords) { Text("기록 더 보기", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

// ---------------- 전화 ----------------
@Composable
fun CallScreen(p: Profile, onCall: (Contact) -> Unit, on119: () -> Unit) {
    Page {
        Title("전화하기")
        Body("이름 옆 버튼을 누르면 전화 화면이 열려요.")
        if (p.contacts.isEmpty()) Card { Body("등록된 연락처가 없어요. 관리자가 관제 사이트에서 등록하면 여기에 나와요.") }
        p.contacts.forEachIndexed { i, c ->
            Card {
                Text("${i + 1}순위 · ${c.rel.ifEmpty { "연락처" }}", fontSize = 18.sp, color = C.sub, fontWeight = FontWeight.SemiBold)
                Text(c.name.ifEmpty { "이름 없음" }, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = C.ink)
                Text(c.phone, fontSize = 24.sp, color = C.ink)
                BigButton("전화 걸기", { onCall(c) }, color = C.ok, icon = Icons.Default.Phone, enabled = c.phone.isNotBlank())
            }
        }
        Card {
            Text("응급 신고", fontSize = 18.sp, color = C.sub, fontWeight = FontWeight.SemiBold)
            Text("119", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = C.crit)
            BigButton("119 전화", on119, color = C.crit, icon = Icons.Default.Call)
        }
    }
}

// ---------------- 기록 ----------------
@Composable
fun RecordsScreen(recs: List<Rec>, onRefresh: () -> Unit) {
    Page {
        Title("기록")
        BigButton("새로 고침", onRefresh, outlined = true, contentColor = C.brand, icon = Icons.Default.Refresh)
        Card {
            if (recs.isEmpty()) Body("아직 기록이 없어요.")
            recs.forEach { RecordRow(it) }
        }
    }
}

// ---------------- 어르신 정보 ----------------
@Composable
fun InfoScreen(p: Profile, onBack: () -> Unit) {
    Page {
        BigButton("돌아가기", onBack, outlined = true, contentColor = C.ink, icon = Icons.Default.ArrowBack)
        Title("어르신 정보")
        Card {
            Text(p.name, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = C.ink)
            val meta = listOf(p.age.takeIf { it.isNotEmpty() }?.let { "${it}세" }, p.sex.ifEmpty { null },
                p.blood.takeIf { it.isNotEmpty() }?.let { "혈액형 $it" }).filterNotNull().joinToString(" · ")
            if (meta.isNotEmpty()) Body(meta, size = 22)
        }
        Card {
            InfoRow("주소", p.addr); InfoRow("질환", p.cond); InfoRow("복용약", p.meds)
            InfoRow("거동", p.mob); InfoRow("특이사항", p.note)
        }
        if (!p.loaded) Body("관제 사이트에 등록된 정보가 아직 없어요.")
        Body("정보 수정은 관리자가 관제 사이트에서 할 수 있어요.", size = 18)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 18.sp, color = C.sub, fontWeight = FontWeight.SemiBold)
        Text(value.ifEmpty { "—" }, fontSize = 22.sp, color = C.ink, lineHeight = 30.sp)
    }
}

// ---------------- 설정 ----------------
@Composable
fun SettingsScreen(
    me: Me?, fontLevel: Int, voice: Boolean, sound: Boolean, node: String,
    needNotify: Boolean, needFullScreen: Boolean, needBattery: Boolean,
    onFont: (Int) -> Unit, onVoice: (Boolean) -> Unit, onSound: (Boolean) -> Unit,
    onTest: () -> Unit, onNotify: () -> Unit, onFullScreen: () -> Unit, onBattery: () -> Unit, onLogout: () -> Unit
) {
    Page {
        Title("설정")
        Card {
            Text("글자 크기", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = C.ink)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("보통", "크게", "아주 크게").forEachIndexed { i, t ->
                    BigButton(t, { onFont(i) }, Modifier.weight(1f), outlined = fontLevel != i,
                        contentColor = if (fontLevel == i) Color.White else C.brand)
                }
            }
        }
        if (needNotify || needFullScreen || needBattery) Card {
            Text("꼭 해 주세요", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = C.crit)
            if (needNotify) { Body("알림이 꺼져 있어서 경보가 안 와요."); BigButton("알림 켜기", onNotify, color = C.crit) }
            if (needFullScreen) { Body("화면이 꺼져 있을 때도 경보 화면이 뜨게 해 주세요."); BigButton("경보 화면 허용하기", onFullScreen, color = C.crit) }
            if (needBattery) { Body("배터리 절약 때문에 앱이 멈추지 않게 해 주세요."); BigButton("배터리 제한 풀기", onBattery, color = C.warn) }
        }
        Card {
            SwitchRow("음성으로 알려주기", "넘어지면 말로 알려 드려요", voice, onVoice)
            SwitchRow("경보음 울리기", "긴급일 때 큰 소리로 울려요", sound, onSound)
            BigButton("경보 시험하기", onTest, outlined = true, contentColor = C.crit, icon = Icons.Default.Notifications)
            Body("시험 경보는 이 휴대폰에서만 울리고, 다른 사람에게는 가지 않아요.", size = 17)
        }
        Card {
            Text("내 계정", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = C.ink)
            if (me != null) { Body("${me.name} (${me.roleText})", color = C.ink, size = 22); Body(me.email, size = 18) }
            Body("센서 이름: $node", size = 18)
            BigButton("로그아웃", onLogout, outlined = true, contentColor = C.sub)
        }
    }
}

@Composable
private fun SwitchRow(title: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = C.ink)
            Text(desc, fontSize = 17.sp, color = C.sub)
        }
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedTrackColor = C.ok))
    }
}

// ---------------- 긴급 경보 화면 (잠금 화면 위에도 뜸) ----------------
@Composable
fun EmergencyScreen(
    name: String, test: Boolean, ackBusy: Boolean, first: Contact?,
    onAck: () -> Unit, onSilence: () -> Unit, onCall: (Contact) -> Unit, on119: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(C.crit).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(96.dp))
        Text(if (test) "시험 경보" else "긴급!", fontSize = 52.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(if (test) "실제 상황이 아니에요. 경보가 잘 울리는지 시험하는 중이에요."
             else "$name 님이 넘어지셨어요.\n움직이지 않으세요.", fontSize = 28.sp, color = Color.White, lineHeight = 38.sp)
        BigButton(if (ackBusy) "보내는 중..." else "확인했어요 (알람 끄기)", onAck, Modifier.heightIn(min = 96.dp),
            color = Color.White, contentColor = C.crit, icon = Icons.Default.Check, enabled = !ackBusy)
        if (first != null) BigButton("${first.rel.ifEmpty { first.name }}에게 전화", { onCall(first) }, color = Color(0xFF7F1D1D), icon = Icons.Default.Phone)
        BigButton("119 전화", on119, color = Color(0xFF7F1D1D), icon = Icons.Default.Call)
        BigButton("소리만 끄기", onSilence, outlined = true, contentColor = Color.White, outlinedContainer = Color.Transparent)
        Text("확인을 누르면 관제 사이트와 다른 보호자에게도 확인했다고 알려져요.", fontSize = 18.sp,
            color = Color.White, textAlign = TextAlign.Start, lineHeight = 26.sp)
    }
}
