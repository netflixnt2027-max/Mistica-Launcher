<?php
session_start();
require __DIR__ . '/config.php';
$erro = '';
if (isset($_POST['senha'])) {
    if (hash_equals(SENHA_ADMIN, (string)$_POST['senha'])) $_SESSION['admin'] = true;
    else $erro = 'Senha incorreta.';
}
if (isset($_POST['sair'])) { session_destroy(); header('Location: index.php'); exit; }
if (!empty($_SESSION['admin']) && isset($_POST['salvar'])) {
    $dados = [
        'gmx' => trim((string)($_POST['gmx'] ?? '')),
        'evento' => trim((string)($_POST['evento'] ?? '')),
        'atualizacoes' => trim((string)($_POST['atualizacoes'] ?? ''))
    ];
    file_put_contents(ARQUIVO_DADOS, json_encode($dados, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE), LOCK_EX);
}
$dados = json_decode(file_get_contents(ARQUIVO_DADOS), true);
?>
<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Painel Mística</title><style>
*{box-sizing:border-box}body{margin:0;background:#10081d;color:#fff;font:16px Arial,sans-serif}.box{max-width:680px;margin:50px auto;padding:28px;background:#211238;border:1px solid #5b347a;border-radius:20px}h1{color:#f5c451;margin-top:0}label{display:block;margin:18px 0 6px;color:#d8cde3}input,textarea{width:100%;padding:13px;background:#140b22;color:#fff;border:1px solid #6d418d;border-radius:10px}textarea{min-height:100px;resize:vertical}button{margin-top:18px;padding:14px 24px;background:#f5c451;color:#160d22;border:0;border-radius:10px;font-weight:bold;cursor:pointer}.erro{color:#ff6877}
</style></head><body><main class="box"><h1>MÍSTICA • Conteúdo do aplicativo</h1>
<?php if (empty($_SESSION['admin'])): ?><p>Entre para editar eventos, GMX e atualizações.</p><form method="post"><label>Senha</label><input type="password" name="senha" required><button>ENTRAR</button><p class="erro"><?=htmlspecialchars($erro)?></p></form>
<?php else: ?><form method="post"><label>Próxima GMX</label><input name="gmx" value="<?=htmlspecialchars($dados['gmx'] ?? '')?>"><label>Evento em destaque</label><textarea name="evento"><?=htmlspecialchars($dados['evento'] ?? '')?></textarea><label>Atualizações</label><textarea name="atualizacoes"><?=htmlspecialchars($dados['atualizacoes'] ?? '')?></textarea><button name="salvar">SALVAR ALTERAÇÕES</button></form><form method="post"><button name="sair">SAIR</button></form><?php endif; ?>
</main></body></html>
