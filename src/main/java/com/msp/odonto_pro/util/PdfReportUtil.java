package com.msp.odonto_pro.util;

import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Helpers compartilhados para geração dos relatórios em PDF (OpenPDF). Evita
 * repetir a configuração de documento/tabela em cada serviço de relatório.
 */
public final class PdfReportUtil {

	public static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
	public static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
	public static final Font CABECALHO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
	public static final Font CELULA = FontFactory.getFont(FontFactory.HELVETICA, 9);

	private PdfReportUtil() {
	}

	public static Document novoDocumento(final OutputStream out) throws DocumentException {
		final Document document = new Document(PageSize.A4, 36, 36, 54, 36);
		PdfWriter.getInstance(document, out);
		document.open();
		return document;
	}

	public static PdfPTable novaTabela(final float[] larguras, final String... cabecalhos) {
		final PdfPTable table = new PdfPTable(cabecalhos.length);
		table.setWidthPercentage(100);
		table.setWidths(larguras);
		for (final String cabecalho : cabecalhos) {
			table.addCell(new PdfPCell(new Phrase(cabecalho, CABECALHO)));
		}
		return table;
	}

	public static void celula(final PdfPTable tabela, final String texto) {
		tabela.addCell(new Phrase(texto != null ? texto : "", CELULA));
	}

	public static Paragraph titulo(final String texto) {
		return new Paragraph(texto, TITULO);
	}

	public static Paragraph subtitulo(final String texto) {
		return new Paragraph(texto, SUBTITULO);
	}

	public static String nullToEmpty(final String valor) {
		return valor != null ? valor : "";
	}
}
