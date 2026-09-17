package com.msp.odonto_pro.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.msp.odonto_pro.dtos.FinanceiroResumoDTO;
import com.msp.odonto_pro.repositorys.ConsultaRepository;
import com.msp.odonto_pro.repositorys.PacienteRepository;
import com.msp.odonto_pro.repositorys.UsuarioRepository;
import com.msp.odonto_pro.util.PdfReportUtil;

import lombok.RequiredArgsConstructor;

/**
 * Relatório consolidado com os KPIs principais do sistema, sempre referentes
 * ao mês/ano atual.
 */
@Service
@RequiredArgsConstructor
public class RelatorioGeralService {

	private final PacienteRepository pacienteRepository;
	private final ConsultaRepository consultaRepository;
	private final UsuarioRepository usuarioRepository;
	private final FinanceiroService financeiroService;

	public byte[] gerarRelatorioPdf() {
		final LocalDate hoje = LocalDate.now();
		final LocalDate inicioMes = hoje.withDayOfMonth(1);
		final LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

		final long totalPacientes = this.pacienteRepository.count();
		final long consultasNoMes = this.consultaRepository.contarPorPeriodo(inicioMes, fimMes);
		final long usuariosAtivos = this.usuarioRepository.countByAtivoTrue();
		final FinanceiroResumoDTO financeiro = this.financeiroService.resumo(hoje.getMonthValue(), hoje.getYear());

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			final Document document = PdfReportUtil.novoDocumento(out);
			document.add(PdfReportUtil.titulo("Relatório Geral - " + String.format("%02d/%d", hoje.getMonthValue(), hoje.getYear())));
			document.add(new Paragraph(" "));

			document.add(new Paragraph("Total de pacientes cadastrados: " + totalPacientes));
			document.add(new Paragraph("Consultas no mês: " + consultasNoMes));
			document.add(new Paragraph(
					String.format(Locale.of("pt", "BR"), "Recebido no mês: R$ %.2f", financeiro.getRecebidoNoMes())));
			document.add(new Paragraph(
					String.format(Locale.of("pt", "BR"), "Total pendente (geral): R$ %.2f", financeiro.getTotalPendente())));
			document.add(new Paragraph("Usuários ativos: " + usuariosAtivos));

			document.close();
			return out.toByteArray();
		} catch (final DocumentException | IOException e) {
			throw new RuntimeException("Erro ao gerar relatório geral", e);
		}
	}
}
