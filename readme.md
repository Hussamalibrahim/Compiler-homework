# مشروع مترجمات: Spring Boot & Thymeleaf Web Application Compiler

| البند | البيان                                                                        |
| :--- |:------------------------------------------------------------------------------|
| **اسم المشروع** | محلل ومترجم لتطبيقات الويب (Spring Boot & Thymeleaf Web Application Compiler) |
| **المادة** | بناء المترجمات (Compiler Construction)                                        |
| **العام الدراسي** | 2026 - 2027                                                                   |
| **رقم المجموعة** | [اكتب رقم المجموعة هنا، مثال: Group 05]                                       |
| **أعضاء المجموعة** | حسام الابراهيم<br/>محمد رزق<br/>عمار الكلاس                                   |

---

## فهرس المحتويات

1. [مقدمة عن المشروع وأهدافه](#1-مقدمة-عن-المشروع-وأهدافه)
2. [التعريف بالتقنيات المستخدمة](#2-التعريف-بالتقنيات-المستخدمة)
3. [التحليل المعجمي والنحوي (Lexer & Parser)](#3-التحليل-المعجمي-والنحوي-lexer--parser)
4. [شجرة القواعد المجردة (AST - Abstract Syntax Tree)](#4-شجرة-القواعد-المجردة-ast---abstract-syntax-tree)
5. [نمط التصميم Visitor (Visitor Pattern)](#5-نمط-التصميم-visitor-visitor-pattern)
6. [جدول الرموز والمجالات (Symbol Table & Scopes)](#6-جدول-الرموز-والمجالات-symbol-table--scopes)
7. [تحليل العلاقات الدلالية (Semantic & Relationship Analysis)](#7-تحليل-العلاقات-الدلالية-semantic--relationship-analysis)
8. [الهندسة المعمارية وفصل الواجهة عن النظام (Architecture & Frontend/Backend Separation)](#8-الهندسة-المعمارية-وفصل-الواجهة-عن-النظام-architecture--frontendbackend-separation)
9. [تطبيق الاختبار المرجعي (Test Project - إدارة المنتجات)](#9-تطبيق-الاختبار-المرجعي-test-project---إدارة-المنتجات)
10. [دليل تشغيل واختبار النظام ومخرجات المقابلة العملية](#10-دليل-تشغيل-واختبار-النظام-ومخرجات-المقابلة-العملية)

---

## 1. مقدمة عن المشروع وأهدافه

يُعد بناء المترجمات (Compiler Construction) أحد أركان علوم الحاسوب الأساسية. يهدف هذا المشروع إلى بناء **مترجم ومحلل لغوي ونحوي ودلالي** لتطبيقات الويب المبنية باستخدام تقنيات **Java و Spring Boot ومحرك القوالب Thymeleaf و HTML و CSS**.

لا يهدف المشروع إلى تحويل الكود إلى لغة آلة تنفيذية، بل يركز على الفهم العميق للبنية البرمجية عبر:
1. **التعرف على التراكيب النحوية والمعجمية** للغات متعددة في نفس التطبيق.
2. **بناء شجرة إعراب نحوية مجردة (AST)** تمثل هيكل المشروع بالكامل وتخزن أرقام الأسطر والمصادر.
3. **تطبيق نمط Visitor** للتجول في شجرة الـ AST وتوليد الطباعة وبناء جدول الرموز.
4. **بناء جدول رموز هرمي (Hierarchical Symbol Table)** يدعم النطاقات (Scopes) المختلفة ويوفر توابع الإضافة، البحث، التعديل، والاستعلام.
5. **التحليل الدلالي للعلاقات** بين متحكمات Spring Boot Controllers وقوالب Thymeleaf للتأكد من صحة الربط، وتطابق المتغيرات، وتحديد الأخطاء في حال وجودها.
6. **بناء واجهة ويب متكاملة حقيقية** مبنية بـ Spring Boot و Thymeleaf تفصل الواجهة تماماً عن كود المترجم (معمارية خالية تماماً من كود JavaScript).

---

## 2. التعريف بالتقنيات المستخدمة

### 2.1 لغة Java
- **الدور**: اللغة الأساسية لبناء نواة المترجم ومكونات النظام.
- **الخصائص المستخدمة**:
  - البرمجة كائنية التوجه (OOP): التجريد (Abstraction)، الوراثة (Inheritance)، وتعدد الأشكال (Polymorphism) في عقد شجرة الـ AST.
  - المعالجة النصية والتعابير النمطية (Regular Expressions) لتقسيم النصوص البرمجية إلى Tokens.
  - Collections Framework و Streams لإدارة الجداول والرموز والتحليل الإحصائي.

### 2.2 إطار العمل Spring Boot
- **الدور**: يوفر بيئة التشغيل المستقلة، وإدارة الاعتماديات، وهندسة MVC (Model-View-Controller).
- **المكونات الأساسية**:
  - `@Controller`: تحديد الفئات المسؤولة عن استقبال طلبات الويب وتوجيه المسارات.
  - `@GetMapping` و `@PostMapping`: ربط دوال المعالجة بمسارات الـ HTTP والـ Endpoints.
  - `Model`: الحاوية المسؤولة عن نقل البيانات من الـ Controller إلى القالب لعرضها ديناميكياً عبر `model.addAttribute(key, value)`.
  - `@Service`: عزل محرك المترجم والتحليل في طبقة خدمات مستقلة كلياً عن الـ Controllers.

### 2.3 محرك القوالب Thymeleaf
- **الدور**: محرك قوالب جانب الخادم (Server-Side Template Engine) لمعالجة صفحات HTML وربط البيانات القادمة من الـ Model.
- **التراكيب البرمجية المدعومة**:
  - `th:text`: إسناد النصوص ديناميكياً لعناصر الـ HTML.
  - `th:each`: حلقة تكرار للمجموعات والمصفوفات مع تعريف متغير التكرار (`product : ${products}`).
  - `th:href` و `th:src`: ربط المسارات والصور والروابط الديناميكية مع تمرير المعاملات.
  - تعبيرات المتغيرات: `${variable.property}`.

### 2.4 لغة HTML
- **الدور**: هيكلة محتوى صفحات الويب عبر عناصر وعلامات (Tags) وخصائص (Attributes). يقوم المحلل باستخراج بنية العناصر وخصائصها وتحديد السطر الخاص بكل عنصر.

### 2.5 لغة CSS
- **الدور**: تحديد التنسيقات البصرية لعناصر الويب. يتعرف المحلل على محددات الأنماط (Selectors) كالفئات والمعرفات والقواعد (Rules) وتصريحات الخصائص والقيم (Declarations).

---

## 3. التحليل المعجمي والنحوي (Lexer & Parser)

تم بناء محللات معجمية ونحوية مستقلة لكل لغة مع الحفاظ على الربط المتكامل داخل المشروع:

```text
Source Files (Java, HTML, CSS)
       │
       ▼
 [ Lexer Engine ]  ───►  Tokens Stream (Token, TokenType, Line, Column)
       │
       ▼
[ Parser Engine ]  ───►  Grammar Validation & AST Nodes Construction
```

### 3.1 المحللات المعجمية (Lexers)
تقوم بقراءة ملفات الشيفرة مصدراً بحرف، وتجميعها في وحدات ذات معنى (`Token`)، مع حفظ:
- نوع الرمز (`TokenType`)
- القيمة النصية (`value`)
- رقم السطر (`lineNumber`) ورقم العمود (`column`)
- اسم الملف المصدري (`sourceFile`)

المحللات المنجزة في الحزمة `com.compiler.lexer`:
- **`JavaLexer`**: يتعرف على الكلمات المحجوزة (`class`, `public`, `return`, `new`)، التعليقات التوضيحية (`@Controller`, `@GetMapping`, `@PostMapping`, `@PathVariable`)، المعرفات، والمعاملات.
- **`HtmlLexer`**: يحلل وسوم HTML وعلامات الفتح والإغلاق والنصوص والخصائص القياسية.
- **`ThymeleafLexer`**: يتعرف على تعبيرات Thymeleaf مثل `${...}` و `th:*` ومتغيرات الحلقات.
- **`CssLexer`**: يحلل محددات CSS، الأقواس المعقوفة `{}`، الخصائص وقيمها، ونقاط الفاصلة المنقوطة.

### 3.2 المحللات النحوية (Parsers)
تقوم المحللات في حزمة `com.compiler.parser` بالتحقق من صحة ترتيب الرموز وفق قواعد الـ Grammar المحددة بصيغة **EBNF**:

#### مثال قواعد EBNF لـ Java Controller:
```ebnf
ControllerClass   ::= [ Annotation ] "public" "class" Identifier "{" { MethodDeclaration | FieldDeclaration } "}" ;
MethodDeclaration ::= [ MappingAnnotation ] "public" ReturnType Identifier "(" [ ParameterList ] ")" "{" { Statement } "}" ;
MappingAnnotation ::= ( "@GetMapping" | "@PostMapping" ) [ "(" StringLiteral ")" ] ;
ReturnStatement   ::= "return" StringLiteral ";" ;
```

#### مثال قواعد EBNF لـ Thymeleaf Template:
```ebnf
TemplateElement   ::= "<" TagName { Attribute | ThymeleafAttribute } ">" { TemplateElement | TextNode } "</" TagName ">" ;
ThymeleafAttribute::= ( "th:text" | "th:each" | "th:href" | "th:src" ) "=" '"' Expression '"' ;
VariableExpression::= "${" Identifier [ "." Identifier ] "}" ;
```

عند وجود أي خطأ نحوي، يقوم الـ Parser بإطلاق استثناء مخصص `ParserException` محتوياً على:
- رسالة الخطأ التوضيحية.
- رقم السطر والعمود الدقيق.
- اسم الملف المصدري المكتشف فيه الخطأ، لتمريره إلى تقرير التحليل `Diagnostic`.

---

## 4. شجرة القواعد المجردة (AST - Abstract Syntax Tree)

### 4.1 البنية البرمجية للشجرة
تم بناء الشجرة بتطبيق كامل لمفاهيم **Object-Oriented Programming (OOP)**:
- **الصنف الأساسي المجرد**: `ASTNode`
  - يحتوي على: `nodeName`, `lineNumber`, `sourceFile`, وقائمة الأبناء `List<ASTNode> children`.
  - التابع المجرد: `public abstract void accept(ASTVisitor visitor);`.
- **التوريث وتعدد الأشكال (Polymorphism)**:
  - كل تركيب نحوي في النظام يرث من `ASTNode` ويوفر تنفيذه الخاص لتابع `accept`.

### 4.2 مخطط بنية كائنات شجرة الـ AST

```
                           ASTNode (Abstract Base Class)
                                     │
    ┌────────────────┬───────────────┴───────────────┬────────────────┐
    │                │                               │                │
ProjectNode      ClassNode                      TemplateNode    StyleSheetNode
                     │                               │                │
             ControllerNode                   HtmlElementNode     CssRuleNode
                     │                               │                │
            ┌────────┴────────┐             ┌────────┴────────┐  CssDeclarationNode
            │                 │             │                 │
        MethodNode      AnnotationNode   TextNode    ThymeleafAttributeNode
            │                                                 │
    ┌───────┴───────┬──────────────────┐             ThymeleafExpressionNode
    │               │                  │
EndpointNode  ParameterNode  ModelAttributeNode
```

### 4.3 تفصيل العقد البرمجية المدعومة:
1. **عقد المشروع وجافا (`com.compiler.ast.java`)**:
   - `ProjectNode`: العقدة الجذرية للمشروع بالكامل.
   - `ClassNode`: تمثل الأصناف البرمجية وحزمها.
   - `ControllerNode`: صنف خاص يرث من `ClassNode` لمتحكمات Spring Boot.
   - `MethodNode`: تمثل الدوال وتوابع المعالجة داخل الـ Controller.
   - `EndpointNode`: تمثل نقاط الـ HTTP (المسار مثل `/products` والنوع `GET/POST`).
   - `ModelAttributeNode`: تمثل البيانات المضافة للـ Model عبر `model.addAttribute("key", expr)`.
   - `ParameterNode`: تمثل معاملات الدوال مثل `Model model` أو `@PathVariable Long id`.
   - `ReturnStatementNode`: تمثل جمل الإرجاع واسم القالب المطلوب عرضه.
2. **عقد القوالب و Thymeleaf (`com.compiler.ast.html`)**:
   - `TemplateNode`: تمثل ملف القالب بالكامل ومساره المنطقي (`products/list.html`).
   - `HtmlElementNode`: تمثل وسوم HTML (`div`, `span`, `header`, `img`, `a`, `form`...).
   - `ThymeleafAttributeNode`: تمثل خصائص Thymeleaf المرفقة بالعنصر.
   - `ThymeleafExpressionNode`: تمثل التعبيرات المحللة داخل الخاصية ومتغيرات الحلقات.
   - `TextNode`: النصوص العادية غير الوسمية.
3. **عقد التنسيقات (`com.compiler.ast.css`)**:
   - `StyleSheetNode`: ملف الـ CSS.
   - `CssRuleNode`: القاعدة التنسيقية.
   - `CssSelectorNode`: المحدد (مثل `.product-card`, `.btn-primary`).
   - `CssDeclarationNode`: الخاصية وقيمتها (مثل `color: #333`).

---

## 5. نمط التصميم Visitor (Visitor Pattern)

يضمن نمط **Visitor** فصل العمليات والخوارزميات عن بنية عقد الشجرة ذاتها:

### 5.1 واجهة الـ Visitor
تحتوي الواجهة `ASTVisitor` على توابع الزيارة التحميلية (Overloaded Methods):
```java
public interface ASTVisitor {
    void visit(ProjectNode node);
    void visit(ClassNode node);
    void visit(ControllerNode node);
    void visit(MethodNode node);
    void visit(EndpointNode node);
    void visit(ModelAttributeNode node);
    void visit(TemplateNode node);
    void visit(HtmlElementNode node);
    void visit(ThymeleafAttributeNode node);
    void visit(StyleSheetNode node);
    // ... لجميع العقد المدعومة
}
```

### 5.2 طباعة شجرة الـ AST (`ASTPrinter`)
يقوم الصنف `ASTPrinter` بزيارة كل عقدة في الشجرة وطباعة هيكلها بشكل هرمي واضح، مع توضيح:
- اسم العقدة ونوعها.
- رقم السطر في الملف الأصلي `[Line X]`.
- اسم الملف المصدري `(filename.java / file.html)`.
- الأبناء متصلين بخطوط هرمية واضحة (`+--`, `\--`).

### 5.3 استخراج الرموز (`SymbolTableVisitor`)
يقوم بالتجول في شجرة الـ AST وتوليد واستخراج جميع الرموز البرمجية تلقائياً وإضافتها إلى جدول الرموز مع تحديد نطاق كل رمز بدقة.

---

## 6. جدول الرموز والمجالات (Symbol Table & Scopes)

يقوم جدول الرموز في حزمة `com.compiler.symboltable` بتنظيم وتخزين كافة المعرفات، الأصناف، الدوال، المسارات، المتغيرات، والقوالب.

### 6.1 بنية الرمز (`Symbol`)
يحتوي كل رمز على:
- `name`: اسم الرمز (مثل اسم الدالة، المتغير، الصنف، مسار الـ Endpoint).
- `type`: نوع الرمز عبر التعداد `SymbolType` (`CLASS`, `CONTROLLER`, `METHOD`, `ENDPOINT`, `MODEL_ATTRIBUTE`, `TEMPLATE`, `TEMPLATE_VARIABLE`, `ITERATION_VARIABLE`, `PARAMETER`, `CSS_SELECTOR`...).
- `scopeName`: اسم المجال الحالي للرمز.
- `sourceFile`: اسم الملف الذي عُرف فيه الرمز.
- `lineNumber`: رقم السطر المصدري.
- `extraDetails`: معلومات تفصيلية (نوع الإرجاع، مسار الـ Endpoint، قيمة التعبير).

### 6.2 إدارة المجالات والنطاقات (`Scope`)
يدعم جدول الرموز شجرة مجالات متداخلة (Hierarchical Scopes):
- **`global`**: النطاق العام للمشروع.
- **`class:ClassName` / `controller:ControllerName`**: نطاق الصنف أو المتحكم.
- **`method:methodName`**: نطاق دالة الـ Controller ومعاملاتها ومتغيرات الـ Model.
- **`template:templateName`**: نطاق القالب والمتغيرات المعروضة فيه.
- **`each:variable`**: نطاق محلي لحلقة التكرار `th:each` لحفظ متغير الدورة.

### 6.3 العمليات المدعومة في `SymbolTable`
- `enterScope(name)` و `exitScope()`: لإدارة الدخول والخروج من النطاقات.
- `addSymbol(symbol)`: إضافة رمز جديد إلى النطاق الحالي.
- `lookup(name)`: البحث عن رمز بدءاً من النطاق الحالي والارتقاء للأعلى نحو النطاق الأب.
- `lookupCurrentScope(name)`: البحث داخل النطاق الحالي فقط.
- `queryByType(type)`: استعلام عن جميع الرموز التي تتبع نوعاً محدداً (مثل استخراج كافة الـ Endpoints).
- `queryByName(name)`: استعلام عن رمز بالاسم عبر كافة النطاقات.
- `printTable()`: طباعة جدول منسق لجميع الرموز المسجلة بالمشروع.

---

## 7. تحليل العلاقات الدلالية (Semantic & Relationship Analysis)

يقوم الصنف `RelationshipAnalyzer` في حزمة `com.compiler.analyzer` بأهم وظائف التحقق الدلالي للربط بين متحكمات Spring Boot وقوالب Thymeleaf:

### 7.1 القواعد الدلالية التي يتم التحقق منها:
1. **التعرف على القوالب وربط المسارات**:
   - فحص القيمة المعادة من دالة الـ Controller (مثل `return "products/list";`).
   - معالجة حالات التوجيه `redirect:/products`.
   - مطابقة اسم القالب مع القوالب المفحوصة والمخزنة في الشجرة.
2. **اكتشاف القوالب المفقودة (`MISSING_TEMPLATE`)**:
   - في حال قامت دالة في الـ Controller بإرجاع قالب غير موجود في مجلد `templates/`، يتم توليد تشخيص خطأ دلالي برقم السطر واسم الملف المصدري.
3. **التحقق من متغيرات الـ Model والربط بالقالب (`UNRESOLVED_VARIABLE`)**:
   - استخراج جميع المتغيرات الممررة في دالة الـ Controller عبر `model.addAttribute("key", ...)`.
   - مطابقتها مع التعبيرات المستدعاة داخل القالب `${key.property}`.
   - التحقق من متغيرات الحلقات `th:each="p : ${products}"`، حيث يُعتبر `products` متغيراً ممرراً من الـ Model، بينما يعتبر `p` متغيراً محلياً صالحاً فقط داخل نطاق العنصر وأبنائه.
   - عند استدعاء متغير في القالب لم يقم الـ Controller بتمريره ولم يُعرّف في حلقة تكرار، يتم توليد خطأ دلالي برقم السطر في ملف الـ HTML.
4. **تنبيهات المتغيرات غير المستخدمة (`UNUSED_MODEL_ATTRIBUTE`)**:
   - توليد تحذير دلالي (`WARNING`) في حال قام الـ Controller بتمرير متغير إلى الـ Model ولكن القالب المستدعى لم يقم باستخدامه أبداً.

---

## 8. الهندسة المعمارية وفصل الواجهة عن النظام (Architecture & Frontend/Backend Separation)

### 8.1 معمارية الـ Backend
- تم بناء المشروع كتطبيق **Spring Boot حقيقي متكامل**.
- محرك المترجم بالكامل يعمل داخل خدمة مستقلة: [`CompilerService`](file:///D:/Programming/Java/springboot/tree-syntax-abstract/src/main/java/com/compiler/service/CompilerService.java).
- المتحكمات [`CompilerWebController`](file:///D:/Programming/Java/springboot/tree-syntax-abstract/src/main/java/com/compiler/controller/CompilerWebController.java) وظيفتها فقط استقبال طلبات الـ HTTP واستدعاء الـ Service وتمرير النتائج عبر `Model` وإرجاع اسم القالب المطلوب (مثل `return "dashboard";` أو `return "ast";`).
- **شرط عدم توليد واجهات في كود جافا**: لا يوجد أي وسم HTML أو أي تنسيق CSS داخل نصوص أو دوال جافا إطلاقاً.

### 8.2 معمارية الـ Frontend (بدون JavaScript - Zero JS Architecture)
تم تخزين كافة ملفات الواجهة بشكل منفصل ومنظم:
- قوالب HTML داخل `src/main/resources/templates/`.
- ملفات CSS داخل `src/main/resources/static/css/`.
- **بدون أي سطر JavaScript**:
  - تم بناء شجرة AST التفاعلية اعتماداً على عناصر **HTML5 الأصلية** (`<details open>` و `<summary>`).
  - التبديل بين الشجرة والنص العادي يتم عبر روابط سيرفر ومعاملات استعلام `th:href="@{/ast(view='tree')}"`.
  - تصفية جدول الرموز وفلاتر العلاقات تتم على السيرفر (Server-side Filtering) عبر استمارات وروابط HTTP GET.
  - تصدير ملف الـ CSV يتم عبر Endpoint مباشر في السيرفر (`GET /symbols/export`).
  - استخدام أجزاء Thymeleaf القابلة لإعادة الاستخدام (`fragments/navbar.html`, `sidebar.html`, `head.html`, `footer.html`).

---

## 9. تطبيق الاختبار المرجعي (Test Project - إدارة المنتجات)

يحتوي مجلد [`test-project/`](file:///D:/Programming/Java/springboot/tree-syntax-abstract/test-project/) على تطبيق تجريبي حقيقي لإدارة المنتجات وفق المواصفات المطلوبة:

### 9.1 بنية كائن المنتج (`Product.java`)
يحتوي الصنف `Product` على الحقول الأربعة المطلوبة:
1. صورة المنتج (`image`)
2. اسم المنتج (`name`)
3. سعر المنتج (`price`)
4. تفاصيل المنتج (`description`)

### 9.2 متحكم المنتجات (`ProductController.java`)
يحتوي على المسارات الثلاثة المطلوبة بالإضافة إلى مسار لاختبار كشف الأخطاء:
1. **عرض المنتجات**:
   - المسار: `GET /products`
   - الدالة: `listProducts(Model model)`
   - القالب المعني: `products/list.html`
   - المتغيرات الممررة: `model.addAttribute("products", productList);`
2. **إضافة منتج**:
   - المسار: `GET /products/add` لعرض النموذج، و `POST /products/add` لمعالجة الحفظ والتوجيه.
   - الدالة: `showAddForm(Model model)` و `addProduct(Product product)`
   - القالب المعني: `products/add.html`
   - المتغيرات الممررة: `model.addAttribute("product", new Product());`
3. **عرض تفاصيل منتج**:
   - المسار: `GET /products/details/{id}`
   - الدالة: `viewProductDetails(@PathVariable Long id, Model model)`
   - القالب المعني: `products/details.html`
   - المتغيرات الممررة: `model.addAttribute("product", found);`
4. **مسار اختبار الأخطاء الدلالية (Error Verification Endpoint)**:
   - المسار: `GET /products/test-error`
   - الدالة: `errorEndpoint(Model model)`
   - إرجاع قالب مفقود متعمد: `return "products/missing_template";` مع متغير غير مستخدم `unusedInfo` لاختبار قدرة المترجم على كشف الأخطاء في التقرير.

---

## 10. دليل تشغيل واختبار النظام ومخرجات المقابلة العملية

### 10.1 متطلبات التشغيل
- تثبيت **Java Development Kit (JDK 21)** أو أحدث.
- وجود Maven Wrapper (`mvnw` / `mvnw.cmd`) المرفق بالمشروع.

### 10.2 تشغيل التطبيق كـ Web Application
من خلال موجه الأوامر (PowerShell أو Terminal):
```powershell
.\mvnw.cmd spring-boot:run
```
ثم فتح المتصفح على العنوان:
```text
http://localhost:8080/
```
سيقوم التطبيق تلقائياً بفتح لوحة التحكم (`/dashboard`) مع تحميل نتائج فحص مشروع `test-project` جاهزة ومكتملة.

### 10.3 تشغيل الاختبارات الآلية (Unit & Integration Tests)
للتأكد من سلامة كافة مكونات المترجم:
```powershell
.\mvnw.cmd test
```
**النتيجة**: تنفيذ 18 اختباراً بنجاح بنسبة 100% دون أي فشل أو أخطاء:
- اختبارات الـ Context ومسارات الويب: `CompilerApplicationTests`, `CompilerWebControllerTest`
- اختبارات Lexer & Parser لـ Java: `JavaLexerParserTest`
- اختبارات Lexer & Parser لـ HTML/Thymeleaf: `HtmlThymeleafParserTest`
- اختبارات جدول الرموز: `SymbolTableTest`
- اختبارات التحليل الدلالي والعلاقات: `RelationshipAnalyzerTest`

### 10.4 التشغيل عبر سطر الأوامر (CLI) لعرض الطباعة أثناء المقابلة
لعرض طباعة الـ AST وجدول الرموز مباشرة في الكونسول أثناء المقابلة العملية:
```powershell
java -cp "target\classes" com.compiler.CompilerMain test-project
```

### 10.5 صفحات واجهة المستخدم المتاحة للاستعراض أثناء المقابلة:
1. **لوحة التحكم (`/dashboard`)**:
   - بطاقات إحصائية للملفات، العقد، الرموز، والمسارات.
   - مؤشر نسبة سلامة العلاقات بين الـ Controllers والقوالب.
   - جدول الملفات المفحوصة وأحجامها وعدد أسطرها.
2. **مستكشف شجرة الـ AST (`/ast`)**:
   - عرض الشجرة التفاعلية القابلة للطي والفتح بدون JS عبر `<details>`.
   - شارات ملونة توضح نوع كل عقدة ورقم السطر والملف المصدري.
   - زر التبديل إلى العرض النصي الكامل للشجرة (ASCII Hierarchy).
3. **مستكشف جدول الرموز (`/symbols`)**:
   - جدول منسق لجميع الرموز والمجالات والمصادر.
   - فلاتر سريعة حسب نوع الرمز ومجال النطاق.
   - إمكانية تصدير جدول الرموز إلى ملف `CSV`.
4. **محلل العلاقات بين المتحكمات والقوالب (`/relationships`)**:
   - بطاقات توضح المسار (`GET/POST`)، دالة الـ Controller، المتغيرات الممررة، القالب المستدعى، وحالة المتغيرات (محلولة أو غير محلولة).
   - تصفية العلاقات الصالحة وغير الصالحة.
5. **تقرير الأخطاء والتشخيص الدلالي (`/analysis`)**:
   - رصد أخطاء الصياغة، القوالب المفقودة، والمتغيرات غير المعرفة بدقة.
6. **اختيار المجلد ورفع المشاريع (`/index`)**:
   - إمكانية تحديد مسار مجلد محلي على الجهاز أو رفع ملف مضغوط `ZIP` لمشروع آخر لتحليله فوراً.
