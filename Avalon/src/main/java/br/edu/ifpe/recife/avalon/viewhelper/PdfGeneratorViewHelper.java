/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifpe.recife.avalon.viewhelper;

import br.edu.ifpe.recife.avalon.model.questao.MultiplaEscolha;
import br.edu.ifpe.recife.avalon.model.questao.Questao;
import br.edu.ifpe.recife.avalon.model.questao.VerdadeiroFalso;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardoamaral
 */
public class PdfGeneratorViewHelper implements Serializable {
    
    Document document;
    Font font;
    
    public void gerarArquivo(OutputStream path, List<Questao> questoes){
        try {
            document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, path);
            
            document.open();
            
            definirFonte();
            imprimirCabecalho();
            imprimirTitulo();
            imprimirQuestoes(questoes);
            
            document.close();
            
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(PdfGeneratorViewHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void definirFonte(){
        font = new Font(Font.FontFamily.TIMES_ROMAN);
    }
    
    private void imprimirCabecalho() throws DocumentException, IOException{
        Image cabecalho = Image.getInstance("/opt/glassfish4/glassfish/domains/domain1/applications/Avalon/resources/img/cabecalho.png");
        
        cabecalho.scalePercent(40.5f, 40.5f);
        
        document.add(cabecalho);
    }
    
    private void imprimirTitulo() throws DocumentException{
        Font fontTitulo = new Font(Font.FontFamily.TIMES_ROMAN, 12.0f, Font.BOLD);
        Paragraph titulo = new Paragraph("Avaliação", fontTitulo);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titulo);
        document.add(Chunk.NEWLINE);
    }
    
    private void imprimirQuestoes(List<Questao> questoes) throws DocumentException, IOException{
        Questao questao;
        for (int i = 0; i < questoes.size(); i++) {
            questao = questoes.get(i);
            
            imprimirEnunciado(questao, i + 1);
            
            if(questao.getImagem() != null){
                imprimirImagem(questao);
            }
            
            imprimirAlternativas(questao);
            document.add(Chunk.NEWLINE);
        }
        
    }
    
    private void imprimirEnunciado(Questao questao, int numero) throws DocumentException{
        Paragraph enunciado = new Paragraph(questao.formatarEnunciado(numero), font);
        document.add(enunciado);
    }
    
    private void imprimirImagem(Questao questao) throws IOException, BadElementException, DocumentException{
        Image imagem = Image.getInstance(questao.getImagem().getArquivo());
        document.add(imagem);
    }

    private void imprimirAlternativas(Questao questao) throws DocumentException, IOException {
        if(questao instanceof VerdadeiroFalso || questao instanceof MultiplaEscolha){
            float indentation = BaseFont.createFont().getWidthPoint(questao.formatarQuestao(), 1.5f);
            Paragraph alternativas = new Paragraph(questao.formatarQuestao(), font);
            alternativas.setIndentationLeft(indentation);
            
            document.add(alternativas);
        }
    }
    
}
