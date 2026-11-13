package com.compiler.controller;

import com.compiler.analyzer.AnalysisReport;
import com.compiler.service.CompilationResult;
import com.compiler.service.CompilerService;
import com.compiler.symboltable.Symbol;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class CompilerWebController {

    private final CompilerService compilerService;

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/index")
    public String index(Model model) {
        CompilationResult result = compilerService.getLastResult();
        model.addAttribute("result", result);
        model.addAttribute("activePage", "index");
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        CompilationResult result = compilerService.getLastResult();
        model.addAttribute("result", result);
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    @GetMapping("/ast")
    public String ast(@RequestParam(name = "view", defaultValue = "tree") String view,
                      Model model) {
        CompilationResult result = compilerService.getLastResult();
        model.addAttribute("result", result);
        model.addAttribute("activePage", "ast");
        model.addAttribute("viewMode", view);
        return "ast";
    }

    @GetMapping("/symbols")
    public String symbols(@RequestParam(name = "type", defaultValue = "ALL") String type,
                          @RequestParam(name = "scope", defaultValue = "ALL") String scope,
                          @RequestParam(name = "search", required = false) String search,
                          Model model) {
        CompilationResult result = compilerService.getLastResult();
        List<Symbol> allSymbols = result.getSymbols();

        List<Symbol> filtered = filterSymbols(allSymbols, type, scope, search);

        model.addAttribute("result", result);
        model.addAttribute("symbols", filtered);
        model.addAttribute("totalCount", allSymbols.size());
        model.addAttribute("filteredCount", filtered.size());
        model.addAttribute("selectedType", type.toUpperCase());
        model.addAttribute("selectedScope", scope);
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("activePage", "symbols");
        return "symbols";
    }

    @GetMapping("/symbols/export")
    public void exportSymbolsCsv(HttpServletResponse response,
                                 @RequestParam(name = "type", defaultValue = "ALL") String type,
                                 @RequestParam(name = "scope", defaultValue = "ALL") String scope,
                                 @RequestParam(name = "search", required = false) String search) throws IOException {
        CompilationResult result = compilerService.getLastResult();
        List<Symbol> filtered = filterSymbols(result.getSymbols(), type, scope, search);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"symbols.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Symbol Name,Type,Scope,Source File,Line,Details");
            for (Symbol s : filtered) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%d\",\"%s\"%n",
                        cleanCsv(s.getName()),
                        cleanCsv(s.getType().name()),
                        cleanCsv(s.getScopeName()),
                        cleanCsv(s.getSourceFile()),
                        s.getLineNumber(),
                        cleanCsv(s.getExtraDetails()));
            }
            writer.flush();
        }
    }

    @GetMapping("/relationships")
    public String relationships(@RequestParam(name = "filter", defaultValue = "ALL") String filter,
                                @RequestParam(name = "search", required = false) String search,
                                Model model) {
        CompilationResult result = compilerService.getLastResult();
        List<AnalysisReport.RelationshipEntry> allRels = result.getRelationships();

        List<AnalysisReport.RelationshipEntry> filtered = allRels.stream()
                .filter(r -> {
                    if ("VALID".equalsIgnoreCase(filter)) return r.isValid();
                    if ("INVALID".equalsIgnoreCase(filter)) return !r.isValid();
                    return true;
                })
                .filter(r -> {
                    if (search == null || search.trim().isEmpty()) return true;
                    String q = search.toLowerCase();
                    return r.getEndpointPath().toLowerCase().contains(q)
                            || r.getControllerName().toLowerCase().contains(q)
                            || r.getMethodName().toLowerCase().contains(q)
                            || r.getReturnedTemplate().toLowerCase().contains(q);
                })
                .collect(Collectors.toList());

        model.addAttribute("result", result);
        model.addAttribute("relationships", filtered);
        model.addAttribute("selectedFilter", filter.toUpperCase());
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("activePage", "relationships");
        return "relationships";
    }

    @GetMapping("/analysis")
    public String analysis(Model model) {
        CompilationResult result = compilerService.getLastResult();
        model.addAttribute("result", result);
        model.addAttribute("activePage", "analysis");
        return "analysis";
    }

    @PostMapping("/compile")
    public String compileProject(@RequestParam(name = "path", defaultValue = "test-project") String path,
                                 RedirectAttributes redirectAttributes) {
        try {
            CompilationResult result = compilerService.compilePath(path);
            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("alertSuccess",
                        "Successfully compiled and analyzed: " + result.getProjectName());
            } else {
                redirectAttributes.addFlashAttribute("alertWarning",
                        "Analysis finished with " + result.getErrorCount() + " error(s) and " + result.getWarningCount() + " warning(s).");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertError", "Compilation failed: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/upload")
    public String uploadProject(@RequestParam("projectFile") MultipartFile projectFile,
                                RedirectAttributes redirectAttributes) {
        if (projectFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("alertError", "Please select a valid ZIP archive to upload.");
            return "redirect:/index";
        }

        try {
            CompilationResult result = compilerService.compileUploadedZip(projectFile);
            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("alertSuccess",
                        "Uploaded project '" + projectFile.getOriginalFilename() + "' compiled successfully!");
            } else {
                redirectAttributes.addFlashAttribute("alertWarning",
                        "Uploaded project analyzed: " + result.getErrorCount() + " error(s) detected.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertError", "Failed to process uploaded file: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    private List<Symbol> filterSymbols(List<Symbol> list, String type, String scope, String search) {
        return list.stream()
                .filter(s -> "ALL".equalsIgnoreCase(type) || s.getType().name().equalsIgnoreCase(type))
                .filter(s -> "ALL".equalsIgnoreCase(scope) || s.getScopeName().equalsIgnoreCase(scope))
                .filter(s -> {
                    if (search == null || search.trim().isEmpty()) return true;
                    String q = search.toLowerCase();
                    return s.getName().toLowerCase().contains(q)
                            || s.getSourceFile().toLowerCase().contains(q)
                            || s.getExtraDetails().toLowerCase().contains(q);
                })
                .collect(Collectors.toList());
    }

    private String cleanCsv(String text) {
        if (text == null) return "";
        return text.replace("\"", "\"\"");
    }
}
